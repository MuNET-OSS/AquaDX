package icu.samnyan.aqua.sega.maimai.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import icu.samnyan.aqua.sega.maimai.handler.impl.*;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author samnyan (privateamusement@protonmail.com)
 */
@RestController
@RequestMapping({"/g/mai", "/g/mai/MaimaiServlet"})
@AllArgsConstructor
public class MaimaiServletController {

    private final GetGameEventHandler getGameEventHandler;
    private final GetGameRankingHandler getGameRankingHandler;
    private final GetGameSettingHandler getGameSettingHandler;
    private final GetTransferFriendHandler getTransferFriendHandler;
    private final GetUserActivityHandler getUserActivityHandler;
    private final GetUserBossHandler getUserBossHandler;
    private final GetUserCharacterHandler getUserCharacterHandler;
    private final GetUserCourseHandler getUserCourseHandler;
    private final GetUserDataHandler getUserDataHandler;
    private final GetUserGradeHandler getUserGradeHandler;
    private final GetUserItemHandler getUserItemHandler;
    private final GetUserMusicHandler getUserMusicHandler;
    private final GetUserOptionHandler getUserOptionHandler;
    private final GetUserPresentEventHandler getUserPresentEventHandler;
    private final GetUserPresentHandler getUserPresentHandler;
    private final GetUserPreviewHandler getUserPreviewHandler;
    private final GetUserRecentRatingHandler getUserRecentRatingHandler;
    private final GetUserSurvivalHandler getUserSurvivalHandler;
    private final GetUserWebOptionHandler getUserWebOptionHandler;
    private final UpsertTransferHandler upsertTransferHandler;
    private final UpsertUserAllHandler upsertUserAllHandler;
    private final UserLoginHandler userLoginHandler;
    private final UserLogoutHandler userLogoutHandler;

    @PostMapping("GetGameEventApi")
    public String getGameEvent(@ModelAttribute Map<String, Object> request) throws JsonProcessingException {
        return getGameEventHandler.handle(request);
    }

    @PostMapping("GetGameRankingApi")
    public String getGameRanking(@ModelAttribute Map<String, Object> request) throws JsonProcessingException {
        return getGameRankingHandler.handle(request);
    }

    @PostMapping("GetGameSettingApi")
    public String getGameSetting(@ModelAttribute Map<String, Object> request, HttpServletRequest http) throws JsonProcessingException {
        request.put("localAddr", http.getLocalAddr());
        request.put("localPort", Integer.toString(http.getLocalPort()));
        // 获取原始请求 URL（保留 /gs/ 路径），并将 GetGameSettingApi 替换为 old
        String originalUrl = http.getHeader("wrapper original url");
        String requestUrl = originalUrl != null ? originalUrl : http.getRequestURL().toString();
        String baseUrl = requestUrl.replace("GetGameSettingApi", "old");
        request.put("baseUrl", baseUrl);
        return getGameSettingHandler.handle(request);
    }

    @PostMapping("GetUserActivityApi")
    public String getUserActivity(@ModelAttribute Map<String, Object> request) throws JsonProcessingException {
        return getUserActivityHandler.handle(request);
    }

    @PostMapping("GetUserBossApi")
    public String getUserBoss(@ModelAttribute Map<String, Object> request) throws JsonProcessingException {
        return getUserBossHandler.handle(request);
    }

    @PostMapping("GetUserCharacterApi")
    public String getUserCharacter(@ModelAttribute Map<String, Object> request) throws JsonProcessingException {
        return getUserCharacterHandler.handle(request);
    }

    @PostMapping("GetUserCourseApi")
    public String getUserCourse(@ModelAttribute Map<String, Object> request) throws JsonProcessingException {
        return getUserCourseHandler.handle(request);
    }

    @PostMapping("GetUserDataApi")
    public String getUserData(@ModelAttribute Map<String, Object> request) throws JsonProcessingException {
        return getUserDataHandler.handle(request);
    }

    @PostMapping("GetTransferFriendApi")
    public String getTransferFriend(@ModelAttribute Map<String, Object> request) throws JsonProcessingException {
        return getTransferFriendHandler.handle(request);
    }

    @PostMapping("GetUserItemApi")
    public String getUserItem(@ModelAttribute Map<String, Object> request) throws JsonProcessingException {
        return getUserItemHandler.handle(request);
    }

    @PostMapping("GetUserMusicApi")
    public String getUserMusic(@ModelAttribute Map<String, Object> request) throws JsonProcessingException {
        return getUserMusicHandler.handle(request);
    }

    @PostMapping("GetUserOptionApi")
    public String getUserOption(@ModelAttribute Map<String, Object> request) throws JsonProcessingException {
        return getUserOptionHandler.handle(request);
    }

    @PostMapping("GetUserPresent")
    public String getUserPresent(@ModelAttribute Map<String, Object> request) throws JsonProcessingException {
        return getUserPresentHandler.handle(request);
    }

    @PostMapping("GetUserPresentEventApi")
    public String getUserPresentEvent(@ModelAttribute Map<String, Object> request) throws JsonProcessingException {
        return getUserPresentEventHandler.handle(request);
    }

    @PostMapping("GetUserPreviewApi")
    public String getUserPreview(@ModelAttribute Map<String, Object> request) throws JsonProcessingException {
        return getUserPreviewHandler.handle((request));
    }

    @PostMapping("GetUserGradeApi")
    public String getUserGrade(@ModelAttribute Map<String, Object> request) throws JsonProcessingException {
        return getUserGradeHandler.handle(request);
    }

    @PostMapping("GetUserRecentRatingApi")
    public String getUserRecentRating(@ModelAttribute Map<String, Object> request) throws JsonProcessingException {
        return getUserRecentRatingHandler.handle(request);
    }

    @PostMapping("GetUserSurvivalApi")
    public String getUserSurvival(@ModelAttribute Map<String, Object> request) throws JsonProcessingException {
        return getUserSurvivalHandler.handle(request);
    }

    @PostMapping("GetUserWebOptionApi")
    public String getUserWebOption(@ModelAttribute Map<String, Object> request) throws JsonProcessingException {
        return getUserWebOptionHandler.handle(request);
    }

    @PostMapping("UpsertTransferApi")
    public String upsertTransfer(@ModelAttribute Map<String, Object> request) throws JsonProcessingException {
        return upsertTransferHandler.handle(request);
    }

    @PostMapping("UpsertUserAllApi")
    public String upsertUserAll(@ModelAttribute Map<String, Object> request) throws JsonProcessingException {
        return upsertUserAllHandler.handle(request);
    }

    @PostMapping("UserLoginApi")
    public String userLogin(@ModelAttribute Map<String, Object> request) throws JsonProcessingException {
        return userLoginHandler.handle(request);
    }

    @PostMapping("UserLogoutApi")
    public String userLogout(@ModelAttribute Map<String, Object> request) throws JsonProcessingException {
        return userLogoutHandler.handle(request);
    }

    @PostMapping("UpsertClientBookkeepingApi")
    public String upsertClientBookkeeping(@ModelAttribute Map<String, Object> request) {
        return "{\"returnCode\":\"1\"}";
    }

    @PostMapping("UpsertClientSettingApi")
    public String upsertClientSetting(@ModelAttribute Map<String, Object> request) {
        return "{\"returnCode\":1,\"apiName\":\"com.sega.maimaiservlet.api.UpsertClientSettingApi\"}";
    }

    @PostMapping("UpsertClientTestmodeApi")
    public String upsertClientTestmode(@ModelAttribute Map<String, Object> request) {
        return "{\"returnCode\":1,\"apiName\":\"com.sega.maimaiservlet.api.UpsertClientTestmodeApi\"}";
    }

    @GetMapping("old/ping")
    public String oldPing(@ModelAttribute Map<String, Object> request) {
        return "ok";
    }

    @GetMapping({"old/{endpoint}/{placeid}/{keychip}/{userid}", "old/{endpoint}/{userid}"})
    public String oldServerUserdata(@ModelAttribute Map<String, Object> request) {
        return "{}";
    }
}
