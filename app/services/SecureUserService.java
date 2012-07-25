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
        return User.loadBySocialUser(SecureSocial.getCurrentUser());
    }

    @Override
    public SocialUser find(UserId id) {
        Long userId = CacheService.getUserId(id.id);
        User user = null;
        if (userId != null) {
            user = User.findById(userId);
        } else {
            user = (User) User.find("username=?", id.id).first();
            if (user != null) {
                CacheService.cacheUserId(user.username, user.id);
            }
        }
        SocialUser socialUser = user != null && user.provider.equals(id.provider) ? user.unpack() : null;
        return socialUser;
    }

    @Override
    public void save(SocialUser socialUser) {
        User user = User.loadBySocialUser(socialUser);
        if (user == null) {
            user = new User();
        }
        user.pack(socialUser);
        user.validateAndSave();
    }

    @Override
    public String createActivation(SocialUser socialUser) {
        User user = User.loadBySocialUser(socialUser);
        if (user != null) {
            return user.verifyCode;
        } else {
            return null;
        }
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
