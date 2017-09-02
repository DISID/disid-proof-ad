package com.disid.ad.web;
import com.disid.ad.model.Profile;
import com.disid.ad.service.api.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jackson.JsonObjectDeserializer;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.convert.ConversionService;
import org.springframework.roo.addon.web.mvc.controller.annotations.config.RooDeserializer;

/**
 * = ProfileDeserializer
 *
 * TODO Auto-generated class documentation
 *
 */
@RooDeserializer(entity = Profile.class)
public class ProfileDeserializer extends JsonObjectDeserializer<Profile> {

    /**
     * TODO Auto-generated attribute documentation
     *
     */
    private ProfileService profileService;

    /**
     * TODO Auto-generated attribute documentation
     *
     */
    private ConversionService conversionService;

    /**
     * TODO Auto-generated constructor documentation
     *
     * @param profileService
     * @param conversionService
     */
    @Autowired
    public ProfileDeserializer(@Lazy ProfileService profileService, ConversionService conversionService) {
        this.profileService = profileService;
        this.conversionService = conversionService;
    }
}
