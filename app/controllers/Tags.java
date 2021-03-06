package controllers;

import models.Participation;
import models.Tag;
import models.User;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: root
 * Date: 4/17/12
 * Time: 8:52 AM
 * To change this template use File | Settings | File Templates.
 */
public class Tags extends Controller {

    /**
     * Check whether the project has the user as a member
     */
    @Before
    static void checkAccess() {
        Projects.checkMembership();
    }

    /**
     * return up to limit # of suggested name tags that contains q
     *
     * @param projectId
     * @param q
     * @param limit
     */
    public static void suggest(Long projectId, String q, Integer limit) {
        if (limit == null) {
            limit = 8;
        }
        if (q.startsWith("@")) {
            q = q.substring(1);
        }

        String returnString = "";

            List<User> users = Participation.find("select p.user from Participation p where p.project.id=? and p.user.username like '%" + StringEscapeUtils.escapeSql(q) + "%'", projectId).fetch(limit);
            for (User user : users) {
                returnString += "@" + user.username + "|" + user.id + "\n";
            }
        renderText(returnString);
    }
}
