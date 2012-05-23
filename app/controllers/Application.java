package controllers;

import controllers.securesocial.SecureSocial;
import play.cache.Cache;
import play.db.jpa.JPA;
import play.i18n.Messages;
import play.libs.Images;
import play.mvc.*;

import models.*;
import securesocial.provider.SocialUser;

import java.util.List;

@With( SecureSocial.class )
public class Application extends Controller {

    public static void index() {
        SocialUser socialUser = SecureSocial.getCurrentUser();
        User user = User.find("byUsername", socialUser.id.id).first();
        List<Project> projects = Projects.getProjects(user);
        if (projects.isEmpty()) {
            projects.add(Projects.addProject(user, Messages.get("myFirstProject")));
        }
        Project selectedProject = projects.get(0);
        render(projects, selectedProject);
    }

    /**
     * http://www.playframework.org/documentation/1.2.4/guide5
     *
     * @param id
     */
    public static void captcha(String id) {
        Images.Captcha captcha = Images.captcha();
        String code = captcha.getText("#E4EAFD");
        Cache.set(id, code, "5mn");
        renderBinary(captcha);
    }

}