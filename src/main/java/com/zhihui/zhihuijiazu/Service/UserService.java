package com.zhihui.zhihuijiazu.Service;

import com.zhihui.zhihuijiazu.Common.util.Result;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public interface UserService {

    Result userLogin(HttpServletRequest request);

    Result userRegister(HttpServletRequest request);

    Result changePassword(HttpServletRequest request);

    Result worksRenovate(HttpServletRequest request);

    Result getUserPerfectionState(HttpServletRequest request);

    Result getUserMsg(HttpServletRequest request);

    Result getSpecialTraining(HttpServletRequest request);

    Result GetSpecialTrainingDetail(HttpServletRequest request);

    Result getFamousTeachers(HttpServletRequest request);

    Result getTeachersId(HttpServletRequest request);

    Result getTeachersType(HttpServletRequest request);

    Result getAudioMaterial(HttpServletRequest request);

    Result getMedicationType(HttpServletRequest request);

    Result getVipDetail(HttpServletRequest request);

    Result getVipFile(HttpServletRequest request);

    Result UserUpate(HttpServletRequest request);

    Result homePageInfo(HttpServletRequest request);

    Result getBuyVip(HttpServletRequest request);

    Result getCoupon(HttpServletRequest request);

    Result getLearningCenter(HttpServletRequest request);

    Result getTrainingList(HttpServletRequest request);

    Result forgetPassword(HttpServletRequest request);

    Result getTrainingDetail(HttpServletRequest request);

    Result getChapterDetail(HttpServletRequest request);

    Result getChapterPrepare(HttpServletRequest request);

    Result getChapterModel(HttpServletRequest request);

    List<Map<String,Object>> findTest();

    Result getTrainingVolumes(HttpServletRequest request);

    Result getDetail(HttpServletRequest request);

    Result getChapterOneTask(HttpServletRequest request);

    Result getChapterOneTaskList(HttpServletRequest request);




}
