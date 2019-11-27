package com.zhihui.zhihuijiazu.Service.impl;

import com.alibaba.fastjson.JSONObject;
import com.zhihui.zhihuijiazu.Common.util.ErrorDict;
import com.zhihui.zhihuijiazu.Common.util.RequestJson;
import com.zhihui.zhihuijiazu.Common.util.Result;
import com.zhihui.zhihuijiazu.Common.util.StringUtils;
import com.zhihui.zhihuijiazu.Dao.CurriculumCategoryMapper;
import com.zhihui.zhihuijiazu.Dao.UserMapper;
import com.zhihui.zhihuijiazu.Dao.UserTokenMapper;
import com.zhihui.zhihuijiazu.Entity.User;
import com.zhihui.zhihuijiazu.Service.CurriculumCategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class CurriculumCategoryServiceImpl implements CurriculumCategoryService {

    @Autowired
    private UserMapper userMapper;
    private CurriculumCategoryMapper curriculumCategoryMapper;

    @Autowired
    private UserTokenMapper userTokenMapper;

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Override
    public Result getSpecialTraining(HttpServletRequest request) {
        try{
            request.setCharacterEncoding("UTF-8");
            String strRequest= RequestJson.parsePostJson(request);
            if(StringUtils.isNull(strRequest)){
                return Result.getResult(0, ErrorDict.DATAERROR);
            }
            JSONObject json=JSONObject.parseObject(strRequest);
            logger.info("获取特训课程接口"+json);
            Integer userId=json.getInteger("User_id");
            String token=json.getString("Token");
            if(!isToken(token,userId)){
                return Result.getSuccessResult(ErrorDict.TOKENERROR);
            }

            Map<String,Object> map=new HashMap<>();
            Map<String,Object> masterMap=curriculumCategoryMapper.getSpecialTraining(userId);

            List<Map<String,Object>> masterList=new ArrayList<>();  //特训课程
            masterList.add(masterMap);

            return Result.getSuccessResultData(ErrorDict.SUCCESS,masterList,map);
        }catch (Exception e){
            e.printStackTrace();
            return Result.getErrorResult(ErrorDict.ERROR);
        }
    }

    public boolean isToken(String token,Integer userId){
//        if(StringUtils.isNull(token)){
//            return false;
//        }
//
//        List<UserToken> userTokens=userTokenMapper.findByUserId(userId);
//        if(userTokens.size()==0){
//            return false;
//        }
//        if(!StringUtils.isEquals(token,userTokens.get(0).getToken())){
//            return false;
//        }
        return true;
    }
}
