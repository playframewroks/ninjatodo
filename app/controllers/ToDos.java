package controllers;

import models.*;
import org.apache.commons.lang.StringUtils;
import play.db.jpa.JPA;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;
import utils.JSConstant;
import utils.Utils;

import javax.persistence.NoResultException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: root
 * Date: 4/15/12
 * Time: 5:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class ToDos extends Controller {
    public static final Long ALL_LIST_ID = -1L;

    /**
     * Check whether the project is visible to the user
     */
    @Before(only = {"loadTasks"})
    static void checkReadAccess() {
        Projects.checkVisibility();
    }

    /**
     * Check whether the project has the user as a member
     */
    @Before(unless = {"loadTasks"})
    static void checkWriteAccess() {
        Projects.checkMembership();
    }

    public static void newTask(Long list, String title) {
        ToDo toDo = new ToDo();
        toDo.title = title;
        toDo.toDoList = ToDoList.findById(list);
        try {
            Integer lastOrderIndex = (Integer) JPA.em()
                    .createQuery("select orderIndex from ToDo order by orderIndex desc")
                    .setMaxResults(1)
                    .getSingleResult();
            toDo.orderIndex = lastOrderIndex + 1;
        } catch (NoResultException nre) {
            // orderIndex should remain the default 0
        }
        toDo.save();
        renderText(Utils.toJson(toDo));
    }
    
    /**
     * clone a toDo. The new toDo will always be incompleted, has its own 
     * created date and last updated date
     */
    public static void cloneTask(Long taskId) {
        ToDo toDo = ToDo.findById(taskId);
        ToDo newToDo = null;
        if (toDo != null) {
            newToDo = new ToDo();
            newToDo.title = toDo.title;
            newToDo.dateDue = toDo.dateDue;
            newToDo.toDoList = toDo.toDoList;
            newToDo.note = toDo.note;
            newToDo.priority = toDo.priority;
            if (toDo.tags != null) {
                newToDo.tags = new ArrayList<Tag>();
                for (Tag tag : toDo.tags) {
                    newToDo.tags.add(tag);
                }
            }
            newToDo.completed = false;
            newToDo.orderIndex = toDo.orderIndex;
            newToDo.save();
        }
        renderText(Utils.toJson(newToDo));
    }

    /**
     * Adds comma separated tags to existing tags of a to do task, does not check for duplicity
     * @param taskId
     * @param tags
     */
    public static void addTag(Long taskId, String tags) {
        ToDo toDo = ToDo.findById(taskId);
        if (toDo != null) {
            addTagToToDo(tags, toDo);
            toDo.save();
        }
        renderText(Utils.toJson(toDo));
    }

    /**
     *
     if(params.search && params.search != '') q += '&s='+encodeURIComponent(params.search);
     if(params.tag && params.tag != '') q += '&t='+encodeURIComponent(params.tag);
     if(params.setCompl && params.setCompl != 0) q += '&setCompl=1';
     q += '&rnd='+Math.random();


     $.getJSON(this.mtt.mttUrl+'loadTasks&list='+params.list+'&completed=' + params.completed + '&changeShowCompleted='+params.changeShowCompleted+'&sort='+params.sort+q, callback);
     */
    public static void loadTasks(Long list, Boolean showCompleted, Boolean changeShowCompleted, String t) {
        if (Boolean.TRUE.equals(changeShowCompleted)) {
            // showCompleted has been changed; update. TODO check permission
            ToDoList toDoList = ToDoList.findById(list);
            if (toDoList != null) {
                toDoList.showCompleted = showCompleted;
                toDoList.save();
            }
        }
        // store
        String sort = params.get("sort") != null && !params.get("sort").equals(JSConstant.STRING_UNDEFINED) ?
                params.get("sort") : null;

        String querySortStr = " order by " + (sort != null ? sort : SortOrder.DEFAULT.getSqlSort());
        // if a valid list id is passed, filter by list, otherwise, filter by project
        String projectFilterStr = "toDoList.project.id=" + params.get("projectId");
        String listFilterStr = "toDoList.id=" + list;
        List<ToDo> tasks = new ArrayList<ToDo>();
        if (StringUtils.isNotEmpty(params.get("s"))) {
            String queryLikeStr = "%"+ params.get("s") + "%";
            String queryStr = (list == ALL_LIST_ID ? projectFilterStr : listFilterStr) +
                    " and (title like ? or note like ?)" + (showCompleted ? "" : " and completed=false");
            tasks = ToDo.find(queryStr + querySortStr, queryLikeStr, queryLikeStr).fetch();
        } else {
            String queryStr = (list == ALL_LIST_ID ? projectFilterStr : listFilterStr) +
                    (showCompleted ? "" : " and completed=false");
            tasks = ToDo.find(queryStr + querySortStr).fetch();
        }
        // filter by tag now
        if (StringUtils.isNotEmpty(t)) {
            String[] tags = t.split(",");
            for (int i=0; i<tasks.size(); i++) {
                // loop through all tags. filtered result must have all tags
                for (String tag : tags) {
                    if (!tasks.get(i).hasTag(tag)) {
                        tasks.remove(i);
                        i--;
                        break;
                    }
                }
            }
        }
        renderText(Utils.toJson(tasks));
    }

    public static void completeTask(Long taskId, boolean completed) {
        ToDo toDo = ToDo.findById(taskId);
        toDo.completed = completed;
        toDo.save();
        renderText(Utils.toJson(toDo));
    }

    /**
     *
     * @param list the primary key of ToDoList
     */
    public static void saveFullTask(Long list, Long taskId) {
        ToDo toDo = null;
        if (taskId == null) {
            toDo = new ToDo();
        } else {
            toDo = ToDo.findById(taskId);
            list = toDo.toDoList.id;
        }
        toDo.title = params.get("title");
        toDo.priority = Integer.parseInt(params.get("prio"));
        toDo.note = params.get("note");
        toDo.toDoList = ToDoList.findById(list);
        if (params.get("duedate") != null && params.get("duedate").length() > 0) {
            try {
                toDo.dateDue = (new SimpleDateFormat("MM/dd/yy")).parse(params.get("duedate"));
            } catch (ParseException pe) {
                // TODO error handling
            }
        }
        // TODO deal with tags
        if (toDo.tags != null) {
            toDo.tags.clear();
        } else {
            toDo.tags = new ArrayList<Tag>();
        }
        addTagToToDo(params.get("tags"), toDo);
        toDo.save();

        renderText(Utils.toJson(toDo));
    }

    /**
     * convenient method to add comma separated tags to todo
     * @param tagsString
     * @param toDo
     */
    private static void addTagToToDo(String tagsString, ToDo toDo) {
        if (StringUtils.isEmpty(params.get("tags"))) {
            return;
        }
        String[] tags = tagsString.split(",");
        for (String t : tags) {
            // first find if tag exists, if not, save it
            t = t.trim();
            // remove empty tag
            if (t.length() > 0) {
                Tag existing = Tag.find("text=? and project=?", t, toDo.toDoList.project).first();
                if (existing == null) {
                    // save this tag if it has not been saved before
                    Tag tag = new Tag();
                    tag.text = t;
                    tag.project = toDo.toDoList.project;
                    tag.save();
                    existing = tag;
                }
                if (toDo.tags == null) {
                    toDo.tags = new ArrayList<Tag>();
                }
                toDo.tags.add(existing);
            }
        }
    }

    /**
     * edit the note of a task
     */
    public static void editNote(Long taskId, String note) {
        ToDo toDo = ToDo.findById(taskId);
        if (toDo != null) {
            toDo.note = note;
            toDo.save();
        }

        renderText(Utils.toJson(toDo));
    }

    /**
     * delete a task completely
     */
    public static void deleteTask(Long taskId) {
        ToDo toDo = ToDo.findById(taskId);
        if (toDo != null) {
            toDo.delete();
        }

        renderText(Utils.toJson(1));
    }

    /**
     * delete a task completely
     */
    public static void setPriority(Long taskId, int prio) {
        ToDo toDo = ToDo.findById(taskId);
        if (toDo != null) {
            toDo.priority = prio;
            toDo.save();
        }

        renderText(Utils.toJson(toDo));
    }

    /**
     * move a task from the current list with id = from to a list with id = to
     * @param taskId
     * @param from
     * @param to
     */
    public static void moveTask(Long taskId, Long from, Long to) {
        ToDo toDo = ToDo.findById(taskId);
        ToDoList newToDoList = ToDoList.findById(to);
        if (toDo != null && toDo.toDoList.id == from && newToDoList != null) {
            toDo.toDoList = newToDoList;
            toDo.save();
        }

        renderText(Utils.toJson(toDo));
    }

    /**
     * changes order of a task
     */
    public static void changeOrder(Long taskId, Long[] back, Long[] forward) {
        if (back != null && back.length > 0) {
            ToDo currentToDo = ToDo.findById(taskId);
            ToDo pushedToDo = ToDo.findById(back[0]);
            if (currentToDo != null && pushedToDo != null) {
                currentToDo.orderIndex = pushedToDo.orderIndex;
            }
            currentToDo.save();
            JPA.em().createQuery("update ToDo t set t.orderIndex = t.orderIndex + 1 where t.id in (" + StringUtils.join(back, ",") + ")")
                    .executeUpdate();
        } else if (forward != null && forward.length > 0) {
            ToDo currentToDo = ToDo.findById(taskId);
            ToDo pushedToDo = ToDo.findById(forward[forward.length - 1]);
            if (currentToDo != null && pushedToDo != null) {
                currentToDo.orderIndex = pushedToDo.orderIndex;
            }
            currentToDo.save();
            JPA.em().createQuery("update ToDo t set t.orderIndex = t.orderIndex - 1 where t.id in (" + StringUtils.join(forward, ",") + ")")
                    .executeUpdate();
        }

        renderText("");
    }
}
