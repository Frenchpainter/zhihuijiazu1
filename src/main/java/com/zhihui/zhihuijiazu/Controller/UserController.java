package com.zhihui.zhihuijiazu.Controller;


import com.zhihui.zhihuijiazu.Common.util.Result;
import com.zhihui.zhihuijiazu.Service.UserService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @RequestMapping("/userTest")
    @ResponseBody
    public Result userLogin(HttpServletRequest request){
        return userService.userLogin(request);
    }

    @RequestMapping(value = "/UserLogin",method = RequestMethod.POST)
    @ResponseBody
    public Result userTest(HttpServletRequest request){
        return userService.userLogin(request);
    }

    @RequestMapping(value = "/HomePageInfo",method = RequestMethod.POST)
    @ResponseBody
    public Result HomePageInfo(HttpServletRequest request){
        return userService.homePageInfo(request);
    }


    @RequestMapping(value = "/UserRegister",method = RequestMethod.POST)
    @ResponseBody
    public Result userRegister(HttpServletRequest request){
        return userService.userRegister(request);
    }

    @RequestMapping(value = "/ChangePassword",method = RequestMethod.POST)
    @ResponseBody
    public Result changePassword(HttpServletRequest request){
        return userService.changePassword(request);
    }


    @RequestMapping(value = "/WorksRenovate",method = RequestMethod.POST)
    @ResponseBody
    public Result worksRenovate(HttpServletRequest request){
        return userService.worksRenovate(request);
    }

    @RequestMapping(value = "/GetUserPerfectionState",method = RequestMethod.POST)
    @ResponseBody
    public Result getUserPerfectionState(HttpServletRequest request){
        return userService.getUserPerfectionState(request);
    }

    @RequestMapping(value = "/GetUserMsg",method = RequestMethod.POST)
    @ResponseBody
    public Result getUserMsg(HttpServletRequest request){
        return userService.getUserMsg(request);
    }

    @RequestMapping(value = "/GetFamousTeachers",method = RequestMethod.POST)
    @ResponseBody
    public Result getFamousTeachers(HttpServletRequest request){
        return userService.getFamousTeachers(request);
    }

    @RequestMapping(value = "/GetTeachersId",method = RequestMethod.POST)
    @ResponseBody
    public Result getTeachersId(HttpServletRequest request){
        return userService.getTeachersId(request);
    }

    @RequestMapping(value = "/GetTeachersType",method = RequestMethod.POST)
    @ResponseBody
    public Result getTeachersType(HttpServletRequest request){
        return userService.getTeachersType(request);
    }

    @RequestMapping(value = "/GetAudioMaterial",method = RequestMethod.POST)
    @ResponseBody
    public Result getAudioMaterial(HttpServletRequest request){
        return userService.getAudioMaterial(request);
    }

    @RequestMapping(value = "/GetMedicationType",method = RequestMethod.POST)
    @ResponseBody
    public Result getMedicationType(HttpServletRequest request){
        return userService.getMedicationType(request);
    }

    @RequestMapping(value = "/GetSpecialTraining",method = RequestMethod.POST)
    @ResponseBody
    public Result getSpecialTraining(HttpServletRequest request){
        return userService.getSpecialTraining(request);
    }

    @RequestMapping(value = "/GetSpecialTrainingDetail",method = RequestMethod.POST)
    @ResponseBody
    public Result GetSpecialTrainingDetail(HttpServletRequest request){
        return userService.GetSpecialTrainingDetail(request);
    }

    @RequestMapping(value = "/GetVipDetail",method = RequestMethod.POST)
    @ResponseBody
    public Result getVipDetail(HttpServletRequest request){
        return userService.getVipDetail(request);
    }

    @RequestMapping(value = "/GetVipFile",method = RequestMethod.POST)
    @ResponseBody
    public Result getVipFile(HttpServletRequest request){
        return userService.getVipFile(request);
    }

    @RequestMapping(value = "/UserUpate",method = RequestMethod.POST)
    @ResponseBody
    public Result userUpate(HttpServletRequest request){
        return userService.UserUpate(request);
    }

    @RequestMapping(value = "/getBuyVip",method = RequestMethod.POST)
    @ResponseBody
    public Result getBuyVip(HttpServletRequest request){
        return userService.getBuyVip(request);
    }

    @RequestMapping(value = "/GetCoupon",method = RequestMethod.POST)
    @ResponseBody
    public Result getCoupon(HttpServletRequest request){
        return userService.getCoupon(request);
    }

    @RequestMapping(value = "/GetLearningCenter",method = RequestMethod.POST)
    @ResponseBody
    public Result getLearningCenter(HttpServletRequest request){
        return userService.getLearningCenter(request);
    }

    @RequestMapping(value = "/GetTrainingDetail",method = RequestMethod.POST)
    @ResponseBody
    public Result getTrainingDetail(HttpServletRequest request){
        return userService.getTrainingDetail(request);
    }

    @RequestMapping(value = "/GetChapterDetail",method = RequestMethod.POST)
    @ResponseBody
    public Result getChapterDetail(HttpServletRequest request){
        return userService.getChapterDetail(request);
    }

    @RequestMapping(value = "/GetChapterPrepare",method = RequestMethod.POST)
    @ResponseBody
    public Result getChapterPrepare(HttpServletRequest request){
        return userService.getChapterPrepare(request);
    }

    @RequestMapping(value = "/GetChapterModel",method = RequestMethod.POST)
    @ResponseBody
    public Result getChapterModel(HttpServletRequest request){
        return userService.getChapterModel(request);
    }

    @RequestMapping(value = "/GetTrainingList",method = RequestMethod.POST)
    @ResponseBody
    public Result getTrainingList(HttpServletRequest request){
        return userService.getTrainingList(request);
    }

    @RequestMapping(value = "/GetTrainingVolumes",method = RequestMethod.POST)
    @ResponseBody
    public Result getTrainingVolumes(HttpServletRequest request){
        return userService.getTrainingVolumes(request);
    }

    @RequestMapping(value = "/GetDetail",method = RequestMethod.POST)
    @ResponseBody
    public Result getDetail(HttpServletRequest request){
        return userService.getDetail(request);
    }

    @RequestMapping(value = "/GetChapterOneTask",method = RequestMethod.POST)
    @ResponseBody
    public Result getChapterOneTask(HttpServletRequest request){
        return userService.getChapterOneTask(request);
    }

    @RequestMapping(value = "/GetChapterOneTaskList",method = RequestMethod.POST)
    @ResponseBody
    public Result getChapterOneTaskList(HttpServletRequest request){ return userService.getChapterOneTaskList(request); }

    @RequestMapping(value = "/GetTest")
    @ResponseBody
    public List<Map<String,Object>> test(HttpServletRequest request){
        return userService.findTest();
    }

}
