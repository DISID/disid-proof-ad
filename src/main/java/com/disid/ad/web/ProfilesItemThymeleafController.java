package com.disid.ad.web;
import com.disid.ad.model.Profile;
import org.springframework.roo.addon.web.mvc.controller.annotations.ControllerType;
import org.springframework.roo.addon.web.mvc.controller.annotations.RooController;
import org.springframework.roo.addon.web.mvc.thymeleaf.annotations.RooThymeleaf;

/**
 * = ProfilesItemThymeleafController
 *
 * TODO Auto-generated class documentation
 *
 */
@RooController(entity = Profile.class, type = ControllerType.ITEM)
@RooThymeleaf
public class ProfilesItemThymeleafController {
}
