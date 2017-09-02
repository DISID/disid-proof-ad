package com.disid.ad.web;
import com.disid.ad.model.Profile;
import org.springframework.roo.addon.web.mvc.controller.annotations.ControllerType;
import org.springframework.roo.addon.web.mvc.controller.annotations.RooController;
import org.springframework.roo.addon.web.mvc.thymeleaf.annotations.RooThymeleaf;

/**
 * = ProfilesCollectionThymeleafController
 *
 * TODO Auto-generated class documentation
 *
 */
@RooController(entity = Profile.class, type = ControllerType.COLLECTION)
@RooThymeleaf
public class ProfilesCollectionThymeleafController {
}
