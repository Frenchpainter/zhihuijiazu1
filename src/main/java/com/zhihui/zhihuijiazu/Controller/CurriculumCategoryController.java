package com.zhihui.zhihuijiazu.Controller;

import com.zhihui.zhihuijiazu.Common.util.Result;
import com.zhihui.zhihuijiazu.Service.CurriculumCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
public class CurriculumCategoryController {

    @Autowired
    private CurriculumCategoryService curriculumCategoryService;

    @RequestMapping("/getSpecialTrainingDataList")
    @ResponseBody
    public Result getSpecialTrainingDataList(HttpServletRequest request){
        return curriculumCategoryService.getSpecialTraining(request);
    }
}
