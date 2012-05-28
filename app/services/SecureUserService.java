package services;

import controllers.securesocial.SecureSocial;
import models.User;
import play.libs.Codec;
import securesocial.provider.SocialUser;
import securesocial.provider.UserId;
import securesocial.provider.UserService;

import java.util.UUID;


/**
 * Created by IntelliJ IDEA.
 * User: sheldon
 * Date: 5/17/12
 * Time: 8:12 AM
 * To change this template use File | Settings | File Templates.
 */
public class SecureUserService implements UserService.Service {
    
    public static User getCurrentUser() {
        SocialUser socialUser = SecureSocial.getCurrentUser();
        User user = User.find("byUsername", socialUser.id.id).first();
        return user;
    }

    @Override
    public SocialUser find(UserId id) {
        User user = User.find("username=? and provider=?", id.id, id.provider).first();
        SocialUser socialUser = user != null ? user.unpack() : null;
        return socialUser;
    }

    @Override
    public void save(SocialUser user) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String createActivation(SocialUser socialUser) {
        User user = new User();
        user.pack(socialUser);
        user.verifyCode = Codec.UUID();
        user.validateAndSave();
        return user.verifyCode;
    }

    @Override
    public boolean activate(String uuid) {
        User user = User.find("verifyCode=?", uuid).first();
        if (user != null) {
            user.isEmailVerified = true;
            user.verifyCode = null;
            user.save();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void deletePendingActivations() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
