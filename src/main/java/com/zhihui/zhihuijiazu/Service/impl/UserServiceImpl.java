package com.zhihui.zhihuijiazu.Service.impl;

import com.alibaba.fastjson.JSONObject;
import com.zhihui.zhihuijiazu.Common.util.*;
import com.zhihui.zhihuijiazu.Dao.UserMapper;
import com.zhihui.zhihuijiazu.Dao.UserTokenMapper;
import com.zhihui.zhihuijiazu.Entity.*;
import com.zhihui.zhihuijiazu.Service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserTokenMapper userTokenMapper;

    @Value("${filePath.ip}")
    private String ip;

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    /**
     * 用户登录
     * @param request
     * @return
     */
    @Override
    public Result userLogin(HttpServletRequest request) {

        try{
            request.setCharacterEncoding("UTF-8");
            String strRequest=RequestJson.parsePostJson(request);
            logger.info("用户登录数据"+strRequest);
            if(StringUtils.isNull(strRequest)){
                return Result.getResult(0,ErrorDict.DATAERROR);
            }

            JSONObject json=JSONObject.parseObject(strRequest);

            String phone=json.getString("Account");
            Integer logintype=json.getInteger("Logintype");
            String password=json.getString("Password");
            Integer clientType=json.getInteger("Clienttype");
            if(StringUtils.isNull(phone)){
                return Result.getResult(0,ErrorDict.DATAERROR);
            }
            List<User> userList=userMapper.findUserByName(phone);
            if(userList.size()==0){
                return Result.getResult(0,ErrorDict.USERNOT);
            }
            if(logintype==1){
                User user=userList.get(0);
                if(user.getStatus()==0){
                    return Result.getResult(0,ErrorDict.USERFROZEN);
                }
                String ciphertext = PasswordEncryption.getEncryptedPassword(password, user.getSalt());
                if(StringUtils.isEquals(ciphertext,user.getPassword())){
                    Map<String,Object> map=new HashMap<>();
                    map.put("user_id",user.getId());
                    if(StringUtils.isNull(user.getName())&&StringUtils.isNull(user.getBirthday())&&StringUtils.isNull(user.getPatriarchname())&&StringUtils.isNull(user.getPatriarchtype())&&StringUtils.isNull(user.getOthermobile())){
                        map.put("perfection","2");
                        map.put("user_name","待完善");
                    }else{
                        map.put("perfection","1");
                        map.put("user_name",user.getName());
                    }
                    map.put("vip_status","1");
                    map.put("user_price",user.getMoney());
                    map.put("account",user.getMobile());

                    UserLog userLog= UserLog.userLogData(user.getId(),StringUtils.newDate(),clientType,null,logintype);
                    userMapper.insertUserLog(userLog);

                    List<UserToken> userTokens=userTokenMapper.findByUserId(user.getId());
                    if(userTokens.size()==0){
                        UserToken userToken=UserToken.getInstance(user.getId());
                        userTokenMapper.insertToken(userToken);
                        map.put("token",userToken.getToken());
                    }else{
                        userTokenMapper.delete(user.getId());

                        UserToken userToken=UserToken.getInstance(user.getId());
                        userTokenMapper.insertToken(userToken);

                        map.put("token",userToken.getToken());
                    }
                    return Result.getSuccessResultData(ErrorDict.SUCCESS,null,map);
                }else{
                    return Result.getResult(0,ErrorDict.PWERROR);
                }
            }
            return Result.getSuccessResult(ErrorDict.SUCCESS);

        }catch (Exception e){
            e.printStackTrace();
            logger.error("用户登录出错"+e);
            return Result.getErrorResult(ErrorDict.ERROR);
        }
    }

    /**
     * 用户注册
     * @param request
     * @return
     */

    @Override
    public Result userRegister(HttpServletRequest request) {
        try{
            request.setCharacterEncoding("UTF-8");
            String strRequest=RequestJson.parsePostJson(request);
            if(StringUtils.isNull(strRequest)){
                return Result.getResult(0,ErrorDict.DATAERROR);
            }
            JSONObject json=JSONObject.parseObject(strRequest);
            logger.info("用户注册数据"+json);
            String phone=json.getString("Account");
            if(StringUtils.isNull(phone)){
                return Result.getResult(0,ErrorDict.DATAERROR);
            }
            List<User> userList=userMapper.findUserByName(phone);
            if(userList.size()>0){
                return Result.getResult(0,ErrorDict.USEREXISIT);
            }

            String salt = PasswordEncryption.generateSalt();
            String ciphertext = PasswordEncryption.getEncryptedPassword(json.getString("Password"), salt);
            User user=new User();
            user.setUsercode("1111");
            user.setUsername(phone);
            user.setRegsource(json.getInteger("Clienttype"));
            user.setMobile(phone);
            user.setInvitationcode(json.getString("Invite_code"));
            user.setPassword(ciphertext);
            user.setSalt(salt);
            user.setEmail("123456@qq.com");
            user.setRegtime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            userMapper.insert(user);
            Map<String,Object> map=new HashMap<>();
            map.put("user_id",user.getId());
            return Result.getSuccessResultData(ErrorDict.SUCCESS,null,map);
        }catch (Exception e){
            e.printStackTrace();
            logger.error("用户注册出错");
            return Result.getErrorResult(ErrorDict.ERROR);
        }
    }

    /**
     * 修改密码
     * @param request
     * @return
     */

    @Override
    public Result changePassword(HttpServletRequest request) {
        try{
            request.setCharacterEncoding("UTF-8");
            String strRequest=RequestJson.parsePostJson(request);
            if(StringUtils.isNull(strRequest)){
                return Result.getResult(0,ErrorDict.DATAERROR);
            }
            JSONObject json=JSONObject.parseObject(strRequest);
            Integer userId=json.getInteger("User_id");
            String token=json.getString("Token");
            if(!isToken(token,userId)){
                return Result.getSuccessResult(ErrorDict.TOKENERROR);
            }

            String oldpassword=json.getString("Oldpassword");
            String newpassword=json.getString("Newpassword");
            String identifying_code=json.getString("Identifying_code");
            if(userId==null||StringUtils.isNull(oldpassword)||StringUtils.isNull(newpassword)||StringUtils.isNull(identifying_code)){
                return Result.getResult(0,ErrorDict.DATAERROR);
            }
            List<User> userList=userMapper.findUserById(userId);
            if(userList.size()==0){
                return Result.getResult(0,ErrorDict.USERNOT);
            }
            User user=userList.get(0);
            String ciphertext = PasswordEncryption.getEncryptedPassword(oldpassword, user.getSalt());
            if(StringUtils.isEquals(ciphertext,user.getPassword())){
                String salt = PasswordEncryption.generateSalt();
                String newciphertext = PasswordEncryption.getEncryptedPassword(newpassword, salt);
                User u=new User();
                u.setSalt(salt);
                u.setPassword(newciphertext);
                u.setId(user.getId());
                userMapper.updatePassword(u);
                return Result.getSuccessResult(ErrorDict.SUCCESS);
            }else{
                return Result.getSuccessResult(ErrorDict.PWERROR);
            }
        }catch (Exception e){
            e.printStackTrace();
            logger.error("修改密码出错");
            return Result.getErrorResult(ErrorDict.ERROR);
        }
    }

    /**
     * 作品推荐
     *
     * @return
     */

    @Override
    public Result worksRenovate(HttpServletRequest request) {
        try{
            request.setCharacterEncoding("UTF-8");
            String strRequest=RequestJson.parsePostJson(request);
            if(StringUtils.isNull(strRequest)){
                return Result.getResult(0,ErrorDict.DATAERROR);
            }
            JSONObject json=JSONObject.parseObject(strRequest);
            Integer userId=json.getInteger("User_id");
            logger.info("作品推荐:userId="+userId);
            String token=json.getString("Token");
            if(!isToken(token,userId)){
                return Result.getSuccessResult(ErrorDict.TOKENERROR);
            }
            Map<String,Object> worksmap=new HashMap<>();
            worksmap.put("title","标题4");
            worksmap.put("pic","http://goss.cfp.cn/creative/vcg/800/new/32a674c435744dbd90f66342010a05b0.jpg?x-oss-process=image/format,jpg/interlace,1");
            worksmap.put("workType","4");
            worksmap.put("type","4");
            worksmap.put("playNum","5000");
            worksmap.put("goodNum","2500");
            worksmap.put("workers","张三");
            worksmap.put("workerPic","http://img4.imgtn.bdimg.com/it/u=2930381777,659240981&fm=15&gp=0.jpg");

            Map<String,Object> worksmap1=new HashMap<>();
            worksmap1.put("title","标题5");
            worksmap1.put("pic","http://goss3.cfp.cn/creative/vcg/800/version23/VCG41498809149.jpg?x-oss-process=image/format,jpg/interlace,1");
            worksmap1.put("workType","5");
            worksmap1.put("type","5");
            worksmap1.put("playNum","6000");
            worksmap1.put("goodNum","3000");
            worksmap1.put("workers","李四");
            worksmap1.put("workerPic","http://img3.imgtn.bdimg.com/it/u=2204147925,1557146352&fm=15&gp=0.jpg");

            Map<String,Object> worksmap2=new HashMap<>();
            worksmap2.put("title","标题6");
            worksmap2.put("pic","http://goss.cfp.cn/creative/vcg/800/version23/VCG41471562191.jpg?x-oss-process=image/format,jpg/interlace,1");
            worksmap2.put("workType","6");
            worksmap2.put("type","6");
            worksmap2.put("playNum","7000");
            worksmap2.put("goodNum","3500");
            worksmap2.put("workers","王五");
            worksmap2.put("workerPic","http://www.qqttxx.com/upimg/allimg/140705/co140F5002228-1.jpg");

            List<Map<String,Object>> worksList=new ArrayList<>();  //作品推荐
            worksList.add(worksmap);
            worksList.add(worksmap1);
            worksList.add(worksmap2);

            return Result.getSuccessResultData(ErrorDict.SUCCESS,worksList,null);
        }catch (Exception e){
            e.printStackTrace();
            logger.error("作品推荐出现错误"+e);
            return Result.getErrorResult(ErrorDict.ERROR);
        }
    }

    /**
     * 获取用户资料完善状态
     * @param request
     * @return
     */

    @Override
    public Result getUserPerfectionState(HttpServletRequest request) {
        try{
            String strRequest=RequestJson.parsePostJson(request);
            if(StringUtils.isNull(strRequest)){
                return Result.getResult(0,ErrorDict.DATAERROR);
            }
            JSONObject json=JSONObject.parseObject(strRequest);
            Integer userId=json.getInteger("User_id");
            logger.info("用户资料完善状态:userId="+userId);
            if(userId==null){
                return Result.getResult(0,ErrorDict.DATAERROR);
            }
            List<User> list=userMapper.findUserById(userId);
            if(list.size()==0){
                return Result.getResult(0,ErrorDict.USERNOT);
            }

            String token=json.getString("Token");
            if(!isToken(token,userId)){
                return Result.getSuccessResult(ErrorDict.TOKENERROR);
            }

            User user=list.get(0);
            Map<String,Object> map=new HashMap<>();
            if(StringUtils.isNull(user.getName())&&StringUtils.isNull(user.getBirthday())&&StringUtils.isNull(user.getPatriarchname())&&StringUtils.isNull(user.getPatriarchtype())&&StringUtils.isNull(user.getOthermobile())){
                map.put("perfection","1");
            }else{
                map.put("perfection",0);
            }
            return Result.getSuccessResultData(ErrorDict.SUCCESS,null,map);
        }catch (Exception e){
            e.printStackTrace();
            logger.error("作品推荐出现错误"+e);
            return Result.getErrorResult(ErrorDict.ERROR);
        }
    }

    /**
     * 获取消息列表
     * @param request
     * @return
     */

    @Override
    public Result getUserMsg(HttpServletRequest request) {
        try{
            request.setCharacterEncoding("UTF-8");
            String strRequest=RequestJson.parsePostJson(request);
            if(StringUtils.isNull(strRequest)){
                return Result.getResult(0,ErrorDict.DATAERROR);
            }
            JSONObject json=JSONObject.parseObject(strRequest);
            logger.info("消息列表"+strRequest);
            Integer userId=json.getInteger("User_id");
            Integer type=json.getInteger("MsgType");

            String token=json.getString("Token");
            if(!isToken(token,userId)){
                return Result.getSuccessResult(ErrorDict.TOKENERROR);
            }

            //消息模拟数据
            UserMsg userMsg=new UserMsg();
            userMsg.setMsgDetailId(1);
            userMsg.setActionId(1);
            userMsg.setActionType(1);
            userMsg.setMsgType(type);
            userMsg.setMsgTitle("消息标题1");
            userMsg.setMsgDesc("消息内容内容内容内容内容内容内容内容内容内容1");
            userMsg.setMsgTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            userMsg.setMsgButton("按钮1");

            UserMsg userMsg1=new UserMsg();
            userMsg1.setMsgDetailId(2);
            userMsg1.setActionId(2);
            userMsg1.setActionType(2);
            userMsg1.setMsgType(type);
            userMsg1.setMsgTitle("消息标题2");
            userMsg1.setMsgDesc("消息内容内容内容内容内容内容内容内容内容内容2");
            userMsg1.setMsgTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            userMsg1.setMsgButton("按钮2");

            UserMsg userMsg2=new UserMsg();
            userMsg2.setMsgDetailId(3);
            userMsg2.setActionId(3);
            userMsg2.setActionType(3);
            userMsg2.setMsgType(type);
            userMsg2.setMsgTitle("消息标题3");
            userMsg2.setMsgDesc("消息内容内容内容内容内容内容内容内容内容内容3");
            userMsg2.setMsgTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            userMsg2.setMsgButton("按钮3");

            List<UserMsg> list=new ArrayList<>();
            list.add(userMsg);
            list.add(userMsg1);
            list.add(userMsg2);

            return Result.getSuccessResultData(ErrorDict.SUCCESS,list,null);
        }catch (Exception e){
            e.printStackTrace();
            logger.error("获取消息列表失败"+e);
            return Result.getErrorResult(ErrorDict.ERROR);
        }
    }

    @Override
    public Result getFamousTeachers(HttpServletRequest request) {
        try{
            request.setCharacterEncoding("UTF-8");
            String strRequest= RequestJson.parsePostJson(request);
            if(StringUtils.isNull(strRequest)){
                return Result.getResult(0, ErrorDict.DATAERROR);
            }
            JSONObject json=JSONObject.parseObject(strRequest);
            logger.info("获取名师精品接口"+json);
            Integer userId=json.getInteger("User_id");
            String token=json.getString("Token");
            if(!isToken(token,userId)){
                return Result.getSuccessResult(ErrorDict.TOKENERROR);
            }

            Map<String,Object> map=new HashMap<>();
            Map<String,Object> masterMap=new HashMap<>();
            masterMap.put("title","标题1");
            masterMap.put("pic","http://goss.cfp.cn/creative/vcg/800/new/32a674c435744dbd90f66342010a05b0.jpg?x-oss-process=image/format,jpg/interlace,1");
            masterMap.put("detailId","1");
            masterMap.put("type","1");
            masterMap.put("learnNum","20");
            masterMap.put("time","40");
            masterMap.put("price","100");
            masterMap.put("teacherName","张三");
            masterMap.put("difficulty","简单");
            masterMap.put("status","1");

            Map<String,Object> masterMap1=new HashMap<>();
            masterMap1.put("title","标题2");
            masterMap1.put("pic","http://goss3.cfp.cn/creative/vcg/800/version23/VCG41498809149.jpg?x-oss-process=image/format,jpg/interlace,1");
            masterMap1.put("detailId","2");
            masterMap1.put("type","2");
            masterMap1.put("learnNum","30");
            masterMap1.put("time","50");
            masterMap1.put("price","200");
            masterMap1.put("teacherName","李四");
            masterMap1.put("difficulty","普通");
            masterMap1.put("status","1");

            Map<String,Object> masterMap2=new HashMap<>();
            masterMap2.put("title","标题3");
            masterMap2.put("pic","http://goss.cfp.cn/creative/vcg/800/version23/VCG41471562191.jpg?x-oss-process=image/format,jpg/interlace,1");
            masterMap2.put("detailId","3");
            masterMap2.put("type","3");
            masterMap2.put("learnNum","40");
            masterMap2.put("time","50");
            masterMap2.put("price","300");
            masterMap2.put("teacherName","王五");
            masterMap2.put("difficulty","困难");
            masterMap2.put("status","1");

            List<Map<String,Object>> masterList=new ArrayList<>();  //名师精品课程
            masterList.add(masterMap);
            masterList.add(masterMap1);
            masterList.add(masterMap2);

            map.put("vipType",0);
            map.put("viewTime","60");
            return Result.getSuccessResultData(ErrorDict.SUCCESS,masterList,map);
        }catch (Exception e){
            e.printStackTrace();
            return Result.getErrorResult(ErrorDict.ERROR);
        }
    }

    @Override
    public Result getTeachersId(HttpServletRequest request) {
        try{
            request.setCharacterEncoding("UTF-8");
            String strRequest= RequestJson.parsePostJson(request);
            if(StringUtils.isNull(strRequest)){
                return Result.getResult(0, ErrorDict.DATAERROR);
            }
            JSONObject json=JSONObject.parseObject(strRequest);
            logger.info("获取名师ID接口"+json);
            Integer userId=json.getInteger("User_id");
            String token=json.getString("Token");
            if(!isToken(token,userId)){
                return Result.getSuccessResult(ErrorDict.TOKENERROR);
            }

            Map<String,Object> map=new HashMap<>();
            map.put("teacherId","1");
            map.put("teacherName","张三");
            map.put("teacherPic","http://hbimg.b0.upaiyun.com/a8a3b1241f1b5182299e01ed8a0166daccc7b72928f9-Gbvwbg_fw658");

            Map<String,Object> map1=new HashMap<>();
            map1.put("teacherId","2");
            map1.put("teacherName","李四");
            map1.put("teacherPic","http://hbimg.b0.upaiyun.com/29cb65c46c8cd67a502f4c67e8b0a696e681780f2d1d-Oq0v61_fw658");

            Map<String,Object> map2=new HashMap<>();
            map2.put("teacherId","3");
            map2.put("teacherName","王五");
            map2.put("teacherPic","http://hbimg.b0.upaiyun.com/5b945b9d1a8176c4c6ad80f9d520cfd1f3d354c09a21-F4WEKj_fw236");

            List<Map<String,Object>> list=new ArrayList<>();
            list.add(map);
            list.add(map1);
            list.add(map2);

            return Result.getSuccessResultData(ErrorDict.SUCCESS,list,null);

        }catch (Exception e){
            e.printStackTrace();
            return Result.getErrorResult(ErrorDict.ERROR);
        }
    }

    @Override
    public Result getTeachersType(HttpServletRequest request) {
        try{
            request.setCharacterEncoding("UTF-8");
            String strRequest= RequestJson.parsePostJson(request);
            if(StringUtils.isNull(strRequest)){
                return Result.getResult(0, ErrorDict.DATAERROR);
            }
            JSONObject json=JSONObject.parseObject(strRequest);
            logger.info("获取名师ID接口"+json);
            Integer userId=json.getInteger("User_id");
            String token=json.getString("Token");
            if(!isToken(token,userId)){
                return Result.getSuccessResult(ErrorDict.TOKENERROR);
            }

            Map<String,Object> map=new HashMap<>();
            map.put("typeId","1");
            map.put("typeName","类型名称1");

            Map<String,Object> map1=new HashMap<>();
            map1.put("typeId","2");
            map1.put("typeName","类型名称2");

            Map<String,Object> map2=new HashMap<>();
            map2.put("typeId","3");
            map2.put("typeName","类型名称3");

            List<Map<String,Object>> list=new ArrayList<>();
            list.add(map);
            list.add(map1);
            list.add(map2);

            return Result.getSuccessResultData(ErrorDict.SUCCESS,list,null);
        }catch (Exception e){
            e.printStackTrace();
            return Result.getErrorResult(ErrorDict.ERROR);
        }
    }

    @Override
    public Result getAudioMaterial(HttpServletRequest request) {
        try{
            request.setCharacterEncoding("UTF-8");
            String strRequest=RequestJson.parsePostJson(request);
            if(StringUtils.isNull(strRequest)){
                return Result.getResult(0,ErrorDict.DATAERROR);
            }
            JSONObject json=JSONObject.parseObject(strRequest);
            logger.info("获取有声读物数据："+json);

            Integer userId=json.getInteger("User_id");
            String token=json.getString("Token");
            if(!isToken(token,userId)){
                return Result.getSuccessResult(ErrorDict.TOKENERROR);
            }

            Map<String,Object> map=new HashMap<>();
            Map<String,Object> audiobooksmap=new HashMap<>();
            audiobooksmap.put("title","标题1");
            audiobooksmap.put("pic","http://goss.cfp.cn/creative/vcg/800/new/32a674c435744dbd90f66342010a05b0.jpg?x-oss-process=image/format,jpg/interlace,1");
            audiobooksmap.put("detailId","1");
            audiobooksmap.put("type","1");
            audiobooksmap.put("learnNum","20");
            audiobooksmap.put("time","40");
            audiobooksmap.put("price","100");
            audiobooksmap.put("difficulty","简单");
            audiobooksmap.put("status","1");

            Map<String,Object> audiobooksmap1=new HashMap<>();
            audiobooksmap1.put("title","标题2");
            audiobooksmap1.put("pic","http://goss3.cfp.cn/creative/vcg/800/version23/VCG41498809149.jpg?x-oss-process=image/format,jpg/interlace,1");
            audiobooksmap1.put("detailId","2");
            audiobooksmap1.put("type","2");
            audiobooksmap1.put("learnNum","30");
            audiobooksmap1.put("time","50");
            audiobooksmap1.put("price","200");
            audiobooksmap1.put("difficulty","普通");
            audiobooksmap1.put("status","1");

            Map<String,Object> audiobooksmap2=new HashMap<>();
            audiobooksmap2.put("title","标题3");
            audiobooksmap2.put("pic","http://goss.cfp.cn/creative/vcg/800/version23/VCG41471562191.jpg?x-oss-process=image/format,jpg/interlace,1");
            audiobooksmap2.put("detailId","3");
            audiobooksmap2.put("type","3");
            audiobooksmap2.put("learnNum","40");
            audiobooksmap2.put("time","50");
            audiobooksmap2.put("price","300");
            audiobooksmap2.put("difficulty","困难");
            audiobooksmap2.put("status","1");

            List<Map<String,Object>> audiobooksList=new ArrayList<>();  //有声读物
            audiobooksList.add(audiobooksmap);
            audiobooksList.add(audiobooksmap1);
            audiobooksList.add(audiobooksmap2);

            map.put("vipType","1");
            map.put("viewTime","100");

            return Result.getSuccessResultData(ErrorDict.SUCCESS,audiobooksList,map);
        }catch (Exception e){
            e.printStackTrace();
            logger.error("获取有声读物接口错误"+e);
            return Result.getErrorResult(ErrorDict.ERROR);
        }
    }

    @Override
    public Result getMedicationType(HttpServletRequest request) {
        try{
            request.setCharacterEncoding("UTF-8");
            String strRequest=RequestJson.parsePostJson(request);
            if(StringUtils.isNull(strRequest)){
                return Result.getResult(0,ErrorDict.DATAERROR);
            }
            JSONObject json=JSONObject.parseObject(strRequest);
            logger.info("数据："+json);

            Integer userId=json.getInteger("User_id");
            String token=json.getString("Token");
            if(!isToken(token,userId)){
                return Result.getSuccessResult(ErrorDict.TOKENERROR);
            }
            Map<String,Object> map=new HashMap<>();
            map.put("typeId","1");
            map.put("typeName","类型名称1");

            Map<String,Object> map1=new HashMap<>();
            map1.put("typeId","2");
            map1.put("typeName","类型名称2");

            Map<String,Object> map2=new HashMap<>();
            map2.put("typeId","3");
            map2.put("typeName","类型名称3");

            List<Map<String,Object>> list=new ArrayList<>();
            list.add(map);
            list.add(map1);
            list.add(map2);

            return Result.getSuccessResultData(ErrorDict.SUCCESS,list,null);

        }catch (Exception e){
            e.printStackTrace();
            return Result.getErrorResult(ErrorDict.ERROR);
        }
    }

    @Override
    public Result getVipDetail(HttpServletRequest request) {
        try{
            request.setCharacterEncoding("UTF-8");
            String strRequest=RequestJson.parsePostJson(request);
            if(StringUtils.isNull(strRequest)){
                return Result.getResult(0,ErrorDict.DATAERROR);
            }
            JSONObject json=JSONObject.parseObject(strRequest);
            logger.info("获取详情："+json);
            Integer userId=json.getInteger("User_id");
            Integer bookType=json.getInteger("BookType");

            Integer detailId=json.getInteger("DetailId");

            String token=json.getString("Token");
            if(!isToken(token,userId)){
                return Result.getSuccessResult(ErrorDict.TOKENERROR);
            }

            Map<String,Object> map=new HashMap<>();
            Map<String,Object> bookinfo=new HashMap<>();
            map.put("pic","http://goss3.cfp.cn/creative/vcg/800/version23/VCG41498809149.jpg?x-oss-process=image/format,jpg/interlace,1");
            map.put("name","课程标题"+userId);
            map.put("type",bookType);
            map.put("detailId",1);
            map.put("learnNum","33");
            map.put("time","55");
            map.put("price","66");
            map.put("difficulty","简单");
            map.put("teacherPic","http://goss.cfp.cn/creative/vcg/800/new/32a674c435744dbd90f66342010a05b0.jpg?x-oss-process=image/format,jpg/interlace,1");
            map.put("teacherName","张三");
            map.put("teacherDesc","简介简介简介简介简介简介简介简介简介简介简介简介简介");
            map.put("program","课程大纲");
            map.put("Object","授课对象");
            map.put("status","1");

            Map<String,Object> map1=new HashMap<>();
            map1.put("id","1");
            map1.put("periodsId","1");
            map1.put("periodsName","课程名称1");
            map1.put("periodsTime","10");
            map1.put("ViewAction","-1");

            Map<String,Object> map2=new HashMap<>();
            map2.put("id","2");
            map2.put("periodsId","2");
            map2.put("periodsName","课程名称2");
            map2.put("periodsTime","20");
            map2.put("ViewAction","-1");

            Map<String,Object> map3=new HashMap<>();
            map3.put("id","3");
            map3.put("periodsId","3");
            map3.put("periodsName","课程名称3");
            map3.put("periodsTime","30");
            map3.put("ViewAction","-1");

            List<Map<String,Object>> list=new ArrayList<>();
            list.add(map1);
            list.add(map2);
            list.add(map3);

            bookinfo.put("Bookinfo",map);
            return Result.getSuccessResultData(ErrorDict.SUCCESS,list,bookinfo);
        }catch (Exception e){
            e.printStackTrace();
            logger.error("获取详情接口错误"+e);
            return Result.getErrorResult(ErrorDict.ERROR);
        }
    }

    @Override
    public Result getVipFile(HttpServletRequest request) {
        try {
            request.setCharacterEncoding("UTF-8");
            String strRequest=RequestJson.parsePostJson(request);
            if(StringUtils.isNull(strRequest)){
                return Result.getResult(0,ErrorDict.DATAERROR);
            }
            JSONObject json=JSONObject.parseObject(strRequest);
            logger.info("获取文件接口："+json);
            Integer userId=json.getInteger("User_id");
            Integer bookType=json.getInteger("BookType");
            Integer periodsId=json.getInteger("PeriodsId");

            String token=json.getString("Token");
            if(!isToken(token,userId)){
                return Result.getSuccessResult(ErrorDict.TOKENERROR);
            }

            Map<String,Object> map=new HashMap<>();
            map.put("file",ip+"/download/10425/1564992237089.jpg");
            return Result.getSuccessResultData(ErrorDict.SUCCESS,null,map);
        }catch (Exception e){
            e.printStackTrace();
            logger.error("获取文件接口错误");
            return Result.getErrorResult(ErrorDict.ERROR);
        }
    }

    @Override
    public Result UserUpate(HttpServletRequest request) {
        try {
            request.setCharacterEncoding("UTF-8");
            String strRequest=RequestJson.parsePostJson(request);
            if(StringUtils.isNull(strRequest)){
                return Result.getResult(0,ErrorDict.DATAERROR);
            }
            JSONObject json=JSONObject.parseObject(strRequest);
            logger.info("完善资料接口："+json);
            Integer userId=json.getInteger("User_id");
            List<User> userList=userMapper.findUserById(userId);
            if(userList.size()==0){
                return Result.getSuccessResult(ErrorDict.USERNOT);
            }

            String token=json.getString("Token");
            if(!isToken(token,userId)){
                return Result.getSuccessResult(ErrorDict.TOKENERROR);
            }

            User user=new User();
            user.setId(userId);
            user.setName(json.getString("Sname"));
            user.setBirthday(json.getString("Sbirthday"));
            user.setPatriarchtype(json.getString("Parentalstatus"));
            user.setPatriarchname(json.getString("Parentalname"));
            user.setOthermobile(json.getString("Parentalphone"));
            if(StringUtils.isNull(userList.get(0).getInvitationcode())){
                user.setInvitationcode(json.getString("Invite_code"));
            }
            userMapper.update(user);
            return Result.getSuccessResult(ErrorDict.SUCCESS);
        }catch (Exception e){
            e.printStackTrace();
            logger.error("完善资料接口错误"+e);
            return Result.getErrorResult(ErrorDict.ERROR);
        }
    }

    @Override
    public Result homePageInfo(HttpServletRequest request) {
        try{
            request.setCharacterEncoding("UTF-8");
            String strRequest=RequestJson.parsePostJson(request);
            if(StringUtils.isNull(strRequest)){
                return Result.getResult(0,ErrorDict.DATAERROR);
            }
            JSONObject json=JSONObject.parseObject(strRequest);
            Integer userId=json.getInteger("User_id");
            if(userId==null){
                return Result.getErrorResult(ErrorDict.DATAERROR);
            }
            String token=json.getString("Token");

            if(!isToken(token,userId)){
                return Result.getSuccessResult(ErrorDict.TOKENERROR);
            }

            Map<String,Object> map=new HashMap<>();
            Map<String,Object> admap=new HashMap<>();
            admap.put("adTitle","标题1");
            admap.put("adPic","http://goss.cfp.cn/creative/vcg/800/new/32a674c435744dbd90f66342010a05b0.jpg?x-oss-process=image/format,jpg/interlace,1");
            admap.put("adId","1");

            Map<String,Object> admap1=new HashMap<>();
            admap1.put("adTitle","标题2");
            admap1.put("adPic","http://goss3.cfp.cn/creative/vcg/800/version23/VCG41498809149.jpg?x-oss-process=image/format,jpg/interlace,1");
            admap1.put("adId","2");

            Map<String,Object> admap2=new HashMap<>();
            admap2.put("adTitle","标题3");
            admap2.put("adPic","http://goss.cfp.cn/creative/vcg/800/version23/VCG41471562191.jpg?x-oss-process=image/format,jpg/interlace,1");
            admap2.put("adId","3");
            List<Map<String,Object>> adList=new ArrayList<>();  //广告轮换图片
            adList.add(admap);
            adList.add(admap1);
            adList.add(admap2);
            map.put("ad_list",adList);

            Map<String,Object> masterMap=new HashMap<>();
            masterMap.put("title","标题1");
            masterMap.put("pic","http://goss.cfp.cn/creative/vcg/800/new/32a674c435744dbd90f66342010a05b0.jpg?x-oss-process=image/format,jpg/interlace,1");
            masterMap.put("detailId","1");
            masterMap.put("type","1");
            masterMap.put("learnNum","20");
            masterMap.put("time","40");
            masterMap.put("price","100");
            masterMap.put("teacherName","张三");
            masterMap.put("difficulty","简单");
            masterMap.put("status","1");

            Map<String,Object> masterMap1=new HashMap<>();
            masterMap1.put("title","标题2");
            masterMap1.put("pic","http://goss3.cfp.cn/creative/vcg/800/version23/VCG41498809149.jpg?x-oss-process=image/format,jpg/interlace,1");
            masterMap1.put("detailId","2");
            masterMap1.put("type","2");
            masterMap1.put("learnNum","30");
            masterMap1.put("time","50");
            masterMap1.put("price","200");
            masterMap1.put("teacherName","李四");
            masterMap1.put("difficulty","普通");
            masterMap1.put("status","1");

            Map<String,Object> masterMap2=new HashMap<>();
            masterMap2.put("title","标题3");
            masterMap2.put("pic","http://goss.cfp.cn/creative/vcg/800/version23/VCG41471562191.jpg?x-oss-process=image/format,jpg/interlace,1");
            masterMap2.put("detailId","3");
            masterMap2.put("type","3");
            masterMap2.put("learnNum","40");
            masterMap2.put("time","50");
            masterMap2.put("price","300");
            masterMap2.put("teacherName","王五");
            masterMap2.put("difficulty","困难");
            masterMap2.put("status","1");

            List<Map<String,Object>> masterList=new ArrayList<>();  //名师精品课程
            masterList.add(masterMap);
            masterList.add(masterMap1);
            masterList.add(masterMap2);
            map.put("master_list",masterList);

            Map<String,Object> audiobooksmap=new HashMap<>();
            audiobooksmap.put("title","标题1");
            audiobooksmap.put("pic","http://goss.cfp.cn/creative/vcg/800/new/32a674c435744dbd90f66342010a05b0.jpg?x-oss-process=image/format,jpg/interlace,1");
            audiobooksmap.put("detailId","1");
            audiobooksmap.put("type","1");
            audiobooksmap.put("learnNum","20");
            audiobooksmap.put("time","40");
            audiobooksmap.put("price","100");
            audiobooksmap.put("difficulty","简单");
            audiobooksmap.put("status","1");

            Map<String,Object> audiobooksmap1=new HashMap<>();
            audiobooksmap1.put("title","标题2");
            audiobooksmap1.put("pic","http://goss3.cfp.cn/creative/vcg/800/version23/VCG41498809149.jpg?x-oss-process=image/format,jpg/interlace,1");
            audiobooksmap1.put("detailId","2");
            audiobooksmap1.put("type","2");
            audiobooksmap1.put("learnNum","30");
            audiobooksmap1.put("time","50");
            audiobooksmap1.put("price","200");
            audiobooksmap1.put("difficulty","普通");
            audiobooksmap1.put("status","1");

            Map<String,Object> audiobooksmap2=new HashMap<>();
            audiobooksmap2.put("title","标题3");
            audiobooksmap2.put("pic","http://goss.cfp.cn/creative/vcg/800/version23/VCG41471562191.jpg?x-oss-process=image/format,jpg/interlace,1");
            audiobooksmap2.put("detailId","3");
            audiobooksmap2.put("type","3");
            audiobooksmap2.put("learnNum","40");
            audiobooksmap2.put("time","50");
            audiobooksmap2.put("price","300");
            audiobooksmap2.put("difficulty","困难");
            audiobooksmap2.put("status","1");

            List<Map<String,Object>> audiobooksList=new ArrayList<>();  //有声读物
            audiobooksList.add(audiobooksmap);
            audiobooksList.add(audiobooksmap1);
            audiobooksList.add(audiobooksmap2);
            map.put("audiobooks_list",audiobooksList);

            Map<String,Object> worksmap=new HashMap<>();
            worksmap.put("title","标题1");
            worksmap.put("pic","http://goss.cfp.cn/creative/vcg/800/new/32a674c435744dbd90f66342010a05b0.jpg?x-oss-process=image/format,jpg/interlace,1");
            worksmap.put("workType","1");
            worksmap.put("type","1");
            worksmap.put("playNum","2000");
            worksmap.put("goodNum","1000");
            worksmap.put("workers","张三");
            worksmap.put("workerPic","http://img4.imgtn.bdimg.com/it/u=2930381777,659240981&fm=15&gp=0.jpg");

            Map<String,Object> worksmap1=new HashMap<>();
            worksmap1.put("title","标题2");
            worksmap1.put("pic","http://goss3.cfp.cn/creative/vcg/800/version23/VCG41498809149.jpg?x-oss-process=image/format,jpg/interlace,1");
            worksmap1.put("workType","2");
            worksmap1.put("type","2");
            worksmap1.put("playNum","3000");
            worksmap1.put("goodNum","1500");
            worksmap1.put("workers","李四");
            worksmap1.put("workerPic","http://img3.imgtn.bdimg.com/it/u=2204147925,1557146352&fm=15&gp=0.jpg");

            Map<String,Object> worksmap2=new HashMap<>();
            worksmap2.put("title","标题3");
            worksmap2.put("pic","http://goss.cfp.cn/creative/vcg/800/version23/VCG41471562191.jpg?x-oss-process=image/format,jpg/interlace,1");
            worksmap2.put("workType","3");
            worksmap2.put("type","3");
            worksmap2.put("playNum","5000");
            worksmap2.put("goodNum","2000");
            worksmap2.put("workers","王五");
            worksmap2.put("workerPic","http://www.qqttxx.com/upimg/allimg/140705/co140F5002228-1.jpg");

            List<Map<String,Object>> worksList=new ArrayList<>();  //作品推荐
            worksList.add(worksmap);
            worksList.add(worksmap1);
            worksList.add(worksmap2);
            map.put("works_list",worksList);

            return Result.getSuccessResultData(ErrorDict.SUCCESS,null,map);
        }catch (Exception e){
            e.printStackTrace();
            logger.error("首页接口错误"+e);
            return Result.getErrorResult(ErrorDict.ERROR);
        }
    }

    @Override
    public Result getBuyVip(HttpServletRequest request) {
        try{
            request.setCharacterEncoding("UTF-8");
            String strRequest= RequestJson.parsePostJson(request);
            if(StringUtils.isNull(strRequest)){
                return Result.getResult(0, ErrorDict.DATAERROR);
            }
            JSONObject json=JSONObject.parseObject(strRequest);
            logger.info("获取购买接口:"+json);
            Integer userId=json.getInteger("User_id");
            Integer bookType=json.getInteger("BookType");
            String token=json.getString("Token");

            if(!isToken(token,userId)){
                return Result.getSuccessResult(ErrorDict.TOKENERROR);
            }

            Map<String,Object> map=new HashMap<>();
            map.put("LimitTime",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            return Result.getSuccessResultData(ErrorDict.SUCCESS,null,map);
        }catch (Exception e){
            e.printStackTrace();
            logger.error("获取购买接口错误"+e);
            return Result.getErrorResult(ErrorDict.ERROR);
        }
    }

    @Override
    public Result getCoupon(HttpServletRequest request) {
        try{
            request.setCharacterEncoding("UTF-8");
            String strRequest= RequestJson.parsePostJson(request);
            if(StringUtils.isNull(strRequest)){
                return Result.getResult(0, ErrorDict.DATAERROR);
            }
            JSONObject json=JSONObject.parseObject(strRequest);
            logger.info("获取购买接口:"+json);
            Integer userId=json.getInteger("User_id");
            if(userId==null){
                return Result.getSuccessResult(ErrorDict.USERNOT);
            }

            String token=json.getString("Token");
            if(!isToken(token,userId)){
                return Result.getSuccessResult(ErrorDict.TOKENERROR);
            }

            List<Map<String,Object>> list=new ArrayList<>();

            Map<String,Object> map=new HashMap<>();
            map.put("couponId","1");
            map.put("couponValue","10");
            map.put("couponInfo","系统发放优惠券1");
            map.put("couponName","系统发放优惠券1");
            map.put("couponDesc","系统发放优惠券1");
            map.put("couponTime",StringUtils.newDate());
            map.put("couponType","1");
            map.put("state","1");

            Map<String,Object> map1=new HashMap<>();
            map1.put("couponId","2");
            map1.put("couponValue","20");
            map1.put("couponInfo","系统发放优惠券2");
            map1.put("couponName","系统发放优惠券2");
            map1.put("couponDesc","系统发放优惠券2");
            map1.put("couponTime",StringUtils.newDate());
            map1.put("couponType","2");
            map1.put("state","2");

            Map<String,Object> map3=new HashMap<>();
            map3.put("couponId","3");
            map3.put("couponValue","30");
            map3.put("couponInfo","系统发放优惠券3");
            map3.put("couponName","系统发放优惠券3");
            map3.put("couponDesc","系统发放优惠券3");
            map3.put("couponTime",StringUtils.newDate());
            map3.put("couponType","3");
            map3.put("state","3");

            list.add(map);
            list.add(map1);
            list.add(map3);

            return Result.getSuccessResultData(ErrorDict.SUCCESS,list,null);

        }catch (Exception e){
            e.printStackTrace();
            logger.error("获取优惠券接口错误"+e);
            return Result.getErrorResult(ErrorDict.ERROR);
        }
    }

    @Override
    public Result getLearningCenter(HttpServletRequest request) {
        try{
            request.setCharacterEncoding("UTF-8");
            String strRequest= RequestJson.parsePostJson(request);
            if(StringUtils.isNull(strRequest)){
                return Result.getResult(0, ErrorDict.DATAERROR);
            }
            JSONObject json=JSONObject.parseObject(strRequest);
            logger.info("获取学习中心接口:"+json);
            Integer userId=json.getInteger("User_id");
            if(userId==null){
                return Result.getSuccessResult(ErrorDict.USERNOT);
            }

            String token=json.getString("Token");
            if(!isToken(token,userId)){
                return Result.getSuccessResult(ErrorDict.TOKENERROR);
            }

            List<Map<String,Object>> list=new ArrayList<>();

            Map<String,Object> map=new HashMap<>();

            Map<String,Object> learningInfo=new HashMap<>();

            Map<String,Object> workInfo=new HashMap<>();
            Map<String,Object> vipInfo=new HashMap<>();

            learningInfo.put("learnedTime","500");
            learningInfo.put("surplusTime","200");
            learningInfo.put("credit","80");
            learningInfo.put("learnedDay","20");

            workInfo.put("finished","30");
            workInfo.put("hiatus","10");
            workInfo.put("good","上榜");

            vipInfo.put("teacherTime","名师精品");
            vipInfo.put("radioTime","有声读物");
            vipInfo.put("parentsTime","家长必读");

            map.put("userVipState","1");
            map.put("userTrainingState","1");
            map.put("learningInfo",learningInfo);
            map.put("workInfo",workInfo);
            map.put("vipInfo",vipInfo);

            Map<String,Object> dataMap=new HashMap<>();

            dataMap.put("title","语文");
            dataMap.put("classTime",StringUtils.newDate());
            dataMap.put("present","10");
            dataMap.put("total","100");
            dataMap.put("state","1");

            Map<String,Object> dataMap1=new HashMap<>();

            dataMap1.put("title","数学");
            dataMap1.put("classTime",StringUtils.newDate());
            dataMap1.put("present","20");
            dataMap1.put("total","200");
            dataMap1.put("state",0);

            Map<String,Object> dataMap2=new HashMap<>();

            dataMap2.put("title","英语");
            dataMap2.put("classTime",StringUtils.newDate());
            dataMap2.put("present","30");
            dataMap2.put("total","300");
            dataMap2.put("state",0);

            list.add(dataMap);
            list.add(dataMap1);
            list.add(dataMap2);

            map.put("trainingDatalist",list);

            return Result.getSuccessResultData(ErrorDict.SUCCESS,null,map);
        }catch (Exception e){
            e.printStackTrace();
            logger.error("获取学习中心接口错误"+e);
            return Result.getErrorResult(ErrorDict.ERROR);
        }
    }

    @Override
    public Result getTrainingList(HttpServletRequest request) {
        try{
            request.setCharacterEncoding("UTF-8");
            String strRequest= RequestJson.parsePostJson(request);
            if(StringUtils.isNull(strRequest)){
                return Result.getResult(0, ErrorDict.DATAERROR);
            }
            JSONObject json=JSONObject.parseObject(strRequest);
            Integer userId=json.getInteger("User_id");
            logger.info("获取特训课程接口userId="+userId);

            String token=json.getString("Token");

            if(!isToken(token,userId)){
                return Result.getSuccessResult(ErrorDict.TOKENERROR);
            }

            Map<String,Object> map=new HashMap<>();

            Map<String,Object> datalist=new HashMap<>();
            datalist.put("pic","http://goss.cfp.cn/creative/vcg/800/new/32a674c435744dbd90f66342010a05b0.jpg?x-oss-process=image/format,jpg/interlace,1");
            datalist.put("name","课程1");
            datalist.put("type","1");
            datalist.put("detailId","1");
            datalist.put("learnNum","10");
            datalist.put("time","10");
            datalist.put("status","0");

            Map<String,Object> datalist1=new HashMap<>();
            datalist1.put("pic","http://goss3.cfp.cn/creative/vcg/800/version23/VCG41498809149.jpg?x-oss-process=image/format,jpg/interlace,1");
            datalist1.put("name","课程2");
            datalist1.put("type","2");
            datalist1.put("detailId","2");
            datalist1.put("learnNum","20");
            datalist1.put("time","20");
            datalist.put("status","0");

            Map<String,Object> datalist2=new HashMap<>();
            datalist2.put("pic","http://goss.cfp.cn/creative/vcg/800/version23/VCG41471562191.jpg?x-oss-process=image/format,jpg/interlace,1");
            datalist2.put("name","课程2");
            datalist2.put("type","2");
            datalist2.put("detailId","2");
            datalist2.put("learnNum","20");
            datalist2.put("time","20");
            datalist.put("status","1");

            map.put("userType",0);
            map.put("learnedTime","25");
            map.put("surplusTime","35");

            List<Map<String,Object>> list=new ArrayList<>();
            list.add(datalist);
            list.add(datalist1);
            list.add(datalist2);
            return Result.getSuccessResultData(ErrorDict.SUCCESS,list,map);
        }catch (Exception e){
            e.printStackTrace();
            logger.error("特训课程接口错误"+e);
            return Result.getErrorResult(ErrorDict.ERROR);
        }
    }

    @Override
    public Result forgetPassword(HttpServletRequest request) {
        try{
            request.setCharacterEncoding("UTF-8");
            String strRequest= RequestJson.parsePostJson(request);
            if(StringUtils.isNull(strRequest)){
                return Result.getResult(0, ErrorDict.DATAERROR);
            }
            JSONObject json=JSONObject.parseObject(strRequest);
            logger.info("忘记密码请求："+json);

            String account=json.getString("Account");
            String password=json.getString("Newpassword");

            String code=json.getString("Identifying_code");
            Integer type=json.getInteger("Clienttype");

            if(StringUtils.isNull(account) || StringUtils.isNull(password) || StringUtils.isNull(code)){
                return Result.getSuccessResult(ErrorDict.DATAERROR);
            }

            List<User> userList=userMapper.findUserByName(account);
            if(userList.size()==0){
                return Result.getSuccessResult(ErrorDict.USERNOT);
            }

            //验证短信验证码



            //生成新密码
            String salt = PasswordEncryption.generateSalt();
            String ciphertext = PasswordEncryption.getEncryptedPassword(password, salt);

            User user=new User();
            user.setSalt(salt);
            user.setPassword(ciphertext);
            user.setId(userList.get(0).getId());
            userMapper.updatePassword(user);
            return Result.getSuccessResult(ErrorDict.SUCCESS);
        }catch (Exception e){
            e.printStackTrace();
            logger.error("忘记密码错误");
            return Result.getErrorResult(ErrorDict.ERROR);
        }
    }

    @Override
    public Result getTrainingDetail(HttpServletRequest request) {
        try{
            request.setCharacterEncoding("UTF-8");
            String strRequest= RequestJson.parsePostJson(request);
            if(StringUtils.isNull(strRequest)){
                return Result.getResult(0, ErrorDict.DATAERROR);
            }
            JSONObject json=JSONObject.parseObject(strRequest);
            Integer userId=json.getInteger("User_id");
            logger.info("获取特训课程详情接口："+json);

            String token=json.getString("Token");

            if(!isToken(token,userId)){
                return Result.getSuccessResult(ErrorDict.TOKENERROR);
            }

            Map<String,Object> trainingInfo=new HashMap<>();
            trainingInfo.put("trainingTitle","语文语文语文语文语文语文");
            trainingInfo.put("trainingType",json.getInteger("TrainingType"));
            trainingInfo.put("trainingDesc","课程预习内容");
            trainingInfo.put("prepareState","1");
            trainingInfo.put("modelState","0");
            trainingInfo.put("time","10:40");
            trainingInfo.put("pic","http://goss2.cfp.cn/creative/vcg/800/new/0601303969464102a35ea1c88305d8db.jpg?x-oss-process=image/format,jpg/interlace,1");
            trainingInfo.put("workStatus","1");

            Map<String,Object> dataDef=new HashMap<>();
            dataDef.put("chapterTitle","标题1");
            dataDef.put("fascicle","第一册");
            dataDef.put("chapterLearner","30");
            dataDef.put("chapterflie","");
            dataDef.put("desc","描述描述描述描述描述描述描述描述描述描述描述描述描述描述描述描述描述描述描述描述描述");

            List<Map<String,Object>> list=new ArrayList<>();

            Map<String,Object> map=new HashMap<>();
            map.put("chapterTitle","第一章");
            map.put("chapterId","1");
            map.put("location","1");
            map.put("picTime","0:37");
            map.put("pic","http://goss2.cfp.cn/creative/vcg/800/new/0601303969464102a35ea1c88305d8db.jpg?x-oss-process=image/format,jpg/interlace,1");
            map.put("chapterName","初唐四杰篇");
            map.put("chapterFile",ip+"/fileview/uploads/20191113/f20a05e6c6ed3f88bfc5811eabeabeac.mp4");

            Map<String,Object> map1=new HashMap<>();
            map1.put("chapterTitle","第二章");
            map1.put("chapterId","2");
            map1.put("location","0");
            map1.put("picTime","0:56");
            map1.put("pic","http://goss2.cfp.cn/creative/vcg/veer/800/new/VCG41N686205730.jpg?x-oss-process=image/format,jpg/interlace,1");
            map1.put("chapterName","梵高篇");
            map1.put("chapterFile",ip+"/fileview/uploads/20191113/f20a05e6c6ed3f88bfc5811eabeabeac.mp4");

            Map<String,Object> map2=new HashMap<>();
            map2.put("chapterTitle","第三章");
            map2.put("chapterId","3");
            map2.put("location","1");
            map2.put("picTime","0:45");
            map2.put("pic","http://goss.cfp.cn/creative/vcg/800/new/VCG211195436972.jpg?x-oss-process=image/format,jpg/interlace,1");
            map2.put("chapterName","XXX篇");
            map2.put("chapterFile",ip+"/fileview/uploads/20191113/f20a05e6c6ed3f88bfc5811eabeabeac.mp4");
            list.add(map);
            list.add(map1);
            list.add(map2);

            Map<String,Object> prepareInfo=new HashMap<>();
            prepareInfo.put("prepareTask","0");
            prepareInfo.put("prepareType","2");
            prepareInfo.put("prepareFile","http://goss.cfp.cn/creative/vcg/800/new/VCG211195436972.jpg?x-oss-process=image/format,jpg/interlace,1");

            Map<String,Object> prepareTaskInfo=new HashMap<>();
            prepareTaskInfo.put("taskTitle","作业名称名称名称");
            prepareTaskInfo.put("taskDesc","简介简介简介简介简介简介简介简介简介简介简介简介简介简介简介简介简介");
            prepareTaskInfo.put("taskAnswered","提醒提醒提醒提醒提醒");
            prepareTaskInfo.put("answeredType","2");
            prepareTaskInfo.put("answeredFile","http://goss.cfp.cn/creative/vcg/800/new/VCG211195436972.jpg?x-oss-process=image/format,jpg/interlace,1");
            prepareInfo.put("prepareTaskInfo",prepareTaskInfo);

            List<Map<String,Object>> list2=new ArrayList<>();

            Map<String,Object> modelmap=new HashMap<>();
            modelmap.put("modelType","1");
            modelmap.put("modelTitle","标题标题标题标题标题1");
            modelmap.put("modelDesc","介绍介绍介绍介绍介绍介绍介绍介绍介绍介绍介绍介绍介绍介绍介绍介绍介绍1");
            modelmap.put("modelFile","modelType是文字");
            modelmap.put("modelPic",ip+"/fileview/uploads/20191113/87ae17f0e97b55199757d442e59071d2.png");
            modelmap.put("modelTime","3:30");

            Map<String,Object> modelmap1=new HashMap<>();
            modelmap1.put("modelType","2");
            modelmap1.put("modelTitle","标题标题标题标题标题2");
            modelmap1.put("modelDesc","介绍介绍介绍介绍介绍介绍介绍介绍介绍介绍介绍介绍介绍介绍介绍介绍介绍2");
            modelmap1.put("modelFile","http://goss.cfp.cn/creative/vcg/800/new/VCG211195436972.jpg?x-oss-process=image/format,jpg/interlace,1");
            modelmap1.put("modelPic",ip+"/fileview/uploads/20191113/87ae17f0e97b55199757d442e59071d2.png");
            modelmap1.put("modelTime","2:30");

            Map<String,Object> modelmap2=new HashMap<>();
            modelmap2.put("modelType","3");
            modelmap2.put("modelTitle","标题标题标题标题标题3");
            modelmap2.put("modelDesc","介绍介绍介绍介绍介绍介绍介绍介绍介绍介绍介绍介绍介绍介绍介绍介绍介绍3");
            modelmap2.put("modelFile","http://goss.cfp.cn/creative/vcg/800/new/VCG211195436972.jpg?x-oss-process=image/format,jpg/interlace,1");
            modelmap2.put("modelPic",ip+"/fileview/uploads/20191113/87ae17f0e97b55199757d442e59071d2.png");
            modelmap2.put("modelTime","1:30");

            list2.add(modelmap);
            list2.add(modelmap1);
            list2.add(modelmap2);

            Map<String,Object> map3=new HashMap<>();
            map3.put("trainingInfo",trainingInfo);
            map3.put("dataDef",dataDef);
            map3.put("chapteList",list);
            map3.put("prepareInfo",prepareInfo);
            map3.put("modelList",list2);

            return Result.getSuccessResultData(ErrorDict.SUCCESS,null,map3);
        }catch (Exception e){
            e.printStackTrace();
            logger.error("获取特训课程详情失败"+e);
            return Result.getErrorResult(ErrorDict.ERROR);
        }
    }

    @Override
    public Result getChapterDetail(HttpServletRequest request) {
        try{
            request.setCharacterEncoding("UTF-8");
            String strRequest= RequestJson.parsePostJson(request);
            if(StringUtils.isNull(strRequest)){
                return Result.getResult(0, ErrorDict.DATAERROR);
            }
            JSONObject json=JSONObject.parseObject(strRequest);
            Integer userId=json.getInteger("User_id");
            logger.info("获取特训课程详情接口："+json);

            String token=json.getString("Token");

            if(!isToken(token,userId)){
                return Result.getSuccessResult(ErrorDict.TOKENERROR);
            }

            Map<String,Object> trainingInfo=new HashMap<>();
            trainingInfo.put("trainingTitle","语文");
            trainingInfo.put("trainingType",json.getInteger("TrainingType"));
            trainingInfo.put("trainingDesc","课程预习内容");
            trainingInfo.put("prepareState","1");
            trainingInfo.put("modelState","0");

            Map<String,Object> dataDef=new HashMap<>();
            dataDef.put("chapterTitle","标题1");
            dataDef.put("fascicle","第一册");
            dataDef.put("chapterLearner","30");
            dataDef.put("chapterflie","");

            Map<String,Object> map3=new HashMap<>();
            map3.put("trainingInfo",trainingInfo);
            map3.put("dataDef",dataDef);

            return Result.getSuccessResultData(ErrorDict.SUCCESS,null,map3);
        }catch (Exception e){
            e.printStackTrace();
            logger.error("获取特训课程详情失败"+e);
            return Result.getErrorResult(ErrorDict.ERROR);
        }
    }

    @Override
    public Result getChapterPrepare(HttpServletRequest request) {
        try{
            request.setCharacterEncoding("UTF-8");
            String strRequest= RequestJson.parsePostJson(request);
            if(StringUtils.isNull(strRequest)){
                return Result.getResult(0, ErrorDict.DATAERROR);
            }
            JSONObject json=JSONObject.parseObject(strRequest);
            Integer userId=json.getInteger("User_id");
            logger.info("获取特训课程预习详情接口："+json);

            String token=json.getString("Token");

            if(!isToken(token,userId)){
                return Result.getSuccessResult(ErrorDict.TOKENERROR);
            }

            Map<String,Object> trainingInfo=new HashMap<>();
            trainingInfo.put("trainingTitle","语文");
            trainingInfo.put("trainingType",json.getInteger("TrainingType"));
            trainingInfo.put("trainingDesc","课程预习内容");
            trainingInfo.put("prepareState","1");
            trainingInfo.put("modelState","0");

            Map<String,Object> prepareTaskInfo=new HashMap<>();
            prepareTaskInfo.put("taskTitle","语文作业");
            prepareTaskInfo.put("taskDesc","语文作业");
            prepareTaskInfo.put("taskAnswered","答案提醒提醒");
            prepareTaskInfo.put("answeredType","1");
            prepareTaskInfo.put("answeredType","");

            Map<String,Object> map3=new HashMap<>();
            map3.put("trainingInfo",trainingInfo);
            map3.put("prepareTaskInfo",prepareTaskInfo);
            map3.put("prepareTask",1);
            map3.put("prepareType",1);
            map3.put("prepareFile","");

            return Result.getSuccessResultData(ErrorDict.SUCCESS,null,map3);
        }catch (Exception e){
            e.printStackTrace();
            logger.error("获取特训课程预习详情接口"+e);
            return Result.getErrorResult(ErrorDict.ERROR);
        }
    }

    @Override
    public Result getChapterModel(HttpServletRequest request) {
        try{
            request.setCharacterEncoding("UTF-8");
            String strRequest= RequestJson.parsePostJson(request);
            if(StringUtils.isNull(strRequest)){
                return Result.getResult(0, ErrorDict.DATAERROR);
            }
            JSONObject json=JSONObject.parseObject(strRequest);
            Integer userId=json.getInteger("User_id");
            logger.info("获取特训课程范读详情接口："+json);

            String token=json.getString("Token");

            if(!isToken(token,userId)){
                return Result.getSuccessResult(ErrorDict.TOKENERROR);
            }

            Map<String,Object> trainingInfo=new HashMap<>();
            trainingInfo.put("trainingTitle","语文");
            trainingInfo.put("trainingType",json.getInteger("TrainingType"));
            trainingInfo.put("trainingDesc","课程预习内容");
            trainingInfo.put("prepareState","1");
            trainingInfo.put("modelState","0");

            Map<String,Object> map3=new HashMap<>();
            map3.put("trainingInfo",trainingInfo);

            List<Map<String,Object>> list=new ArrayList<>();

            Map<String,Object> map=new HashMap<>();
            map.put("modelType","1");
            map.put("modelTitle","标题标题1");
            map.put("modelDesc","介绍介绍介绍介绍介绍介绍1");
            map.put("modelFile","");

            Map<String,Object> map1=new HashMap<>();
            map1.put("modelType","2");
            map1.put("modelTitle","标题标题2");
            map1.put("modelDesc","介绍介绍介绍介绍介绍介绍2");
            map1.put("modelFile","");

            Map<String,Object> map2=new HashMap<>();
            map2.put("modelType","3");
            map2.put("modelTitle","标题标题3");
            map2.put("modelDesc","介绍介绍介绍介绍介绍介绍3");
            map2.put("modelFile","");

            list.add(map);
            list.add(map1);
            list.add(map2);

            return Result.getSuccessResultData(ErrorDict.SUCCESS,list,map3);
        }catch (Exception e){
            e.printStackTrace();
            logger.error("获取特训课程范读详情接口"+e);
            return Result.getErrorResult(ErrorDict.ERROR);
        }
    }

    @Override
    public List<Map<String, Object>> findTest() {
        return userMapper.findTest();
    }

    @Override
    public Result getTrainingVolumes(HttpServletRequest request) {
        try{
            request.setCharacterEncoding("UTF-8");
            String strRequest= RequestJson.parsePostJson(request);
            if(StringUtils.isNull(strRequest)){
                return Result.getResult(0, ErrorDict.DATAERROR);
            }
            JSONObject json=JSONObject.parseObject(strRequest);
            Integer userId=json.getInteger("User_id");
            logger.info("获取特训课程接口userId="+userId);

            String token=json.getString("Token");

            if(!isToken(token,userId)){
                return Result.getSuccessResult(ErrorDict.TOKENERROR);
            }
            Integer traningType=json.getInteger("TraningType");

            Map<String,Object> map=new HashMap<>();

            Map<String,Object> learningInfo=new HashMap<>();
            learningInfo.put("classPic","http://goss3.cfp.cn/creative/vcg/800/version23/VCG41498809149.jpg?x-oss-process=image/format,jpg/interlace,1");
            learningInfo.put("classContent","上课内容内容内容");
            learningInfo.put("classTime","2019-11-13 14:00:00");
            learningInfo.put("learnStatus","0");
            learningInfo.put("learnContent","学习信息信息信息");

            map.put("userType","-1");
            map.put("learningInfo",learningInfo);

            List<Map<String,Object>> list=new ArrayList<>();

            Map<String,Object> dataList=new HashMap<>();
            dataList.put("volumesName","第一册");
            dataList.put("volumesInfo","第一册第一册第一册第一册第一册");

            List<Object> volumeList=new ArrayList<>();

            Map<String,Object> volumeMap=new HashMap<>();
            volumeMap.put("pic","http://goss3.cfp.cn/creative/vcg/800/version23/VCG41498809149.jpg?x-oss-process=image/format,jpg/interlace,1");
            volumeMap.put("name","课程标题1");
            volumeMap.put("detailId","1");
            volumeMap.put("num","1");

            Map<String,Object> volumeMap1=new HashMap<>();
            volumeMap1.put("pic","http://goss3.cfp.cn/creative/vcg/800/version23/VCG41498809149.jpg?x-oss-process=image/format,jpg/interlace,1");
            volumeMap1.put("name","课程标题2");
            volumeMap1.put("detailId","2");
            volumeMap1.put("num","2");

            Map<String,Object> volumeMap2=new HashMap<>();
            volumeMap2.put("pic","http://goss3.cfp.cn/creative/vcg/800/version23/VCG41498809149.jpg?x-oss-process=image/format,jpg/interlace,1");
            volumeMap2.put("name","课程标题3");
            volumeMap2.put("detailId","3");
            volumeMap2.put("num","3");

            Map<String,Object> volumeMap3=new HashMap<>();
            volumeMap3.put("pic","http://goss3.cfp.cn/creative/vcg/800/version23/VCG41498809149.jpg?x-oss-process=image/format,jpg/interlace,1");
            volumeMap3.put("name","课程标题4");
            volumeMap3.put("detailId","4");
            volumeMap3.put("num","4");

            Map<String,Object> volumeMap4=new HashMap<>();
            volumeMap4.put("pic","http://goss3.cfp.cn/creative/vcg/800/version23/VCG41498809149.jpg?x-oss-process=image/format,jpg/interlace,1");
            volumeMap4.put("name","课程标题5");
            volumeMap4.put("detailId","5");
            volumeMap4.put("num","5");

            Map<String,Object> volumeMap5=new HashMap<>();
            volumeMap5.put("pic","http://goss3.cfp.cn/creative/vcg/800/version23/VCG41498809149.jpg?x-oss-process=image/format,jpg/interlace,1");
            volumeMap5.put("name","课程标题6");
            volumeMap5.put("detailId","6");
            volumeMap5.put("num","6");

            volumeList.add(volumeMap);
            volumeList.add(volumeMap1);
            volumeList.add(volumeMap2);

            dataList.put("volumesList",volumeList);

            Map<String,Object> dataList2=new HashMap<>();
            dataList2.put("volumesName","第二册");
            dataList2.put("volumesInfo","第二册第二册第二册第二册第二册");

            List<Object> volumeList2=new ArrayList<>();
            volumeList2.add(volumeMap3);
            volumeList2.add(volumeMap4);
            volumeList2.add(volumeMap5);

            dataList2.put("volumesList",volumeList2);

            list.add(dataList);
            list.add(dataList2);

            return Result.getSuccessResultData(ErrorDict.SUCCESS,list,map);

        }catch (Exception e){
            e.printStackTrace();
            logger.error("获取全部课程错误");
            return Result.getErrorResult(ErrorDict.ERROR);
        }
    }

    @Override
    public Result getDetail(HttpServletRequest request) {
        try{
            request.setCharacterEncoding("UTF-8");
            String strRequest= RequestJson.parsePostJson(request);
            if(StringUtils.isNull(strRequest)){
                return Result.getResult(0, ErrorDict.DATAERROR);
            }
            JSONObject json=JSONObject.parseObject(strRequest);
            Integer userId=json.getInteger("User_id");
            logger.info("获取特训课程详情接口："+json);

            String token=json.getString("Token");

            if(!isToken(token,userId)){
                return Result.getSuccessResult(ErrorDict.TOKENERROR);
            }

            Map<String,Object> trainingInfo=new HashMap<>();
            trainingInfo.put("trainingTitle","语文语文语文语文语文语文");
            trainingInfo.put("trainingType",json.getInteger("TrainingType"));
            trainingInfo.put("trainingDesc","课程预习内容");
            trainingInfo.put("prepareState","1");
            trainingInfo.put("modelState","0");
            trainingInfo.put("time","10:40");
            trainingInfo.put("pic","http://goss2.cfp.cn/creative/vcg/800/new/0601303969464102a35ea1c88305d8db.jpg?x-oss-process=image/format,jpg/interlace,1");
            trainingInfo.put("workStatus","1");

            Map<String,Object> dataDef=new HashMap<>();
            dataDef.put("chapterTitle","标题1");
            dataDef.put("fascicle","第一册");
            dataDef.put("chapterLearner","30");
            dataDef.put("chapterflie","");
            dataDef.put("desc","描述描述描述描述描述描述描述描述描述描述描述描述描述描述描述描述描述描述描述描述描述");

            List<Map<String,Object>> list=new ArrayList<>();

            Map<String,Object> map=new HashMap<>();
            map.put("chapterTitle","第一章");
            map.put("chapterId","1");
            map.put("location","1");
            map.put("picTime","0:37");
            map.put("pic","http://goss2.cfp.cn/creative/vcg/800/new/0601303969464102a35ea1c88305d8db.jpg?x-oss-process=image/format,jpg/interlace,1");
            map.put("chapterName","初唐四杰篇");

            Map<String,Object> map1=new HashMap<>();
            map1.put("chapterTitle","第二章");
            map1.put("chapterId","2");
            map1.put("location","0");
            map1.put("picTime","0:56");
            map1.put("pic","http://goss2.cfp.cn/creative/vcg/veer/800/new/VCG41N686205730.jpg?x-oss-process=image/format,jpg/interlace,1");
            map1.put("chapterName","梵高篇");
            Map<String,Object> map2=new HashMap<>();
            map2.put("chapterTitle","第三章");
            map2.put("chapterId","3");
            map2.put("location","0");
            map2.put("picTime","0:45");
            map2.put("pic","http://goss.cfp.cn/creative/vcg/800/new/VCG211195436972.jpg?x-oss-process=image/format,jpg/interlace,1");
            map2.put("chapterName","XXX篇");
            list.add(map);
            list.add(map1);
            list.add(map2);

            Map<String,Object> prepareInfo=new HashMap<>();
            prepareInfo.put("prepareTask","0");
            prepareInfo.put("prepareType","2");
            prepareInfo.put("prepareFile","http://goss.cfp.cn/creative/vcg/800/new/VCG211195436972.jpg?x-oss-process=image/format,jpg/interlace,1");

            Map<String,Object> prepareTaskInfo=new HashMap<>();
            prepareTaskInfo.put("taskTitle","作业名称名称名称");
            prepareTaskInfo.put("taskDesc","简介简介简介简介简介简介简介简介简介简介简介简介简介简介简介简介简介");
            prepareTaskInfo.put("taskAnswered","提醒提醒提醒提醒提醒");
            prepareTaskInfo.put("answeredType","2");
            prepareTaskInfo.put("answeredFile","http://goss.cfp.cn/creative/vcg/800/new/VCG211195436972.jpg?x-oss-process=image/format,jpg/interlace,1");
            prepareInfo.put("prepareTaskInfo",prepareTaskInfo);

            List<Map<String,Object>> list2=new ArrayList<>();

            Map<String,Object> modelmap=new HashMap<>();
            modelmap.put("modelType","1");
            modelmap.put("modelTitle","标题标题标题标题标题1");
            modelmap.put("modelDesc","介绍介绍介绍介绍介绍介绍介绍介绍介绍介绍介绍介绍介绍介绍介绍介绍介绍1");
            modelmap.put("modelFile","modelType是文字");
            modelmap.put("modelPic",ip+"/fileview/uploads/20191113/87ae17f0e97b55199757d442e59071d2.png");
            modelmap.put("modelTime","3:30");

            Map<String,Object> modelmap1=new HashMap<>();
            modelmap1.put("modelType","2");
            modelmap1.put("modelTitle","标题标题标题标题标题2");
            modelmap1.put("modelDesc","介绍介绍介绍介绍介绍介绍介绍介绍介绍介绍介绍介绍介绍介绍介绍介绍介绍2");
            modelmap1.put("modelFile","http://goss.cfp.cn/creative/vcg/800/new/VCG211195436972.jpg?x-oss-process=image/format,jpg/interlace,1");
            modelmap1.put("modelPic",ip+"/fileview/uploads/20191113/87ae17f0e97b55199757d442e59071d2.png");
            modelmap1.put("modelTime","2:30");

            Map<String,Object> modelmap2=new HashMap<>();
            modelmap2.put("modelType","3");
            modelmap2.put("modelTitle","标题标题标题标题标题3");
            modelmap2.put("modelDesc","介绍介绍介绍介绍介绍介绍介绍介绍介绍介绍介绍介绍介绍介绍介绍介绍介绍3");
            modelmap2.put("modelFile","http://goss.cfp.cn/creative/vcg/800/new/VCG211195436972.jpg?x-oss-process=image/format,jpg/interlace,1");
            modelmap2.put("modelPic",ip+"/fileview/uploads/20191113/87ae17f0e97b55199757d442e59071d2.png");
            modelmap2.put("modelTime","1:30");

            list2.add(modelmap);
            list2.add(modelmap1);
            list2.add(modelmap2);

            Map<String,Object> map3=new HashMap<>();
            map3.put("trainingInfo",trainingInfo);
            map3.put("dataDef",dataDef);
            map3.put("prepareInfo",prepareInfo);
            map3.put("modelList",list2);

            return Result.getSuccessResultData(ErrorDict.SUCCESS,null,map3);
        }catch (Exception e){
            e.printStackTrace();
            logger.error("获取特训课程详情失败"+e);
            return Result.getErrorResult(ErrorDict.ERROR);
        }
    }

    @Override
    public Result getChapterOneTask(HttpServletRequest request) {
        try{
            request.setCharacterEncoding("UTF-8");
            String strRequest= RequestJson.parsePostJson(request);
            if(StringUtils.isNull(strRequest)){
                return Result.getResult(0, ErrorDict.DATAERROR);
            }
            JSONObject json=JSONObject.parseObject(strRequest);
            Integer userId=json.getInteger("User_id");
            logger.info("获取特训课程接口userId="+userId);

            String token=json.getString("Token");

            if(!isToken(token,userId)){
                return Result.getSuccessResult(ErrorDict.TOKENERROR);
            }

            Integer chapterId=json.getInteger("ChapterId");
            Integer trainingType=json.getInteger("TrainingType");

            if(chapterId==null || trainingType==null){
                return Result.getSuccessResult(ErrorDict.DATAERROR);
            }

            List<Object> list=new ArrayList<>();
            Map<String,Object> map=new HashMap<>();
            map.put("teacherjob_id",1);
            map.put("quesstion","作业问题1");
            map.put("status",-1);

            Map<String,Object> map1=new HashMap<>();
            map1.put("teacherjob_id",2);
            map1.put("quesstion","作业问题2");
            map1.put("status",0);

            Map<String,Object> map2=new HashMap<>();
            map2.put("teacherjob_id",3);
            map2.put("quesstion","作业问题3");

            map2.put("status",0);
            List<Object> option=new ArrayList<>();
            Map<String,Object> optionmap= new HashMap<>();
            Map<String,Object> optionmap1= new HashMap<>();
            Map<String,Object> optionmap2= new HashMap<>();
            Map<String,Object> optionmap3= new HashMap<>();

            map.put("questionType",trainingType);
            map1.put("questionType",trainingType);
            map2.put("questionType",trainingType);

            if(trainingType==3){
                optionmap.put("type","video");
                optionmap.put("value",ip+"/fileview/uploads/20191113/f20a05e6c6ed3f88bfc5811eabeabeac.mp4");
                option.add(optionmap);
                map.put("questionOptions", option);
                map.put("answerKey",3);
                map1.put("questionOptions", option);
                map2.put("questionOptions", option);
            }else if(trainingType==6){
                optionmap.put("type","video");
                optionmap.put("value",ip+"/fileview/uploads/20191113/f20a05e6c6ed3f88bfc5811eabeabeac.mp4");
                option.add(optionmap);
                option.add(optionmap);
                option.add(optionmap);

                map.put("questionOptions", option);
                map.put("answerKey","2");

                map1.put("questionOptions", option);
                map2.put("questionOptions", option);
            }else if(trainingType==2){
                optionmap.put("type","image");
                optionmap.put("value",ip+"/fileview/uploads/20191113/87ae17f0e97b55199757d442e59071d2.png");
                option.add(optionmap);
                map.put("questionOptions", option);
                map.put("answerKey",1);
                map1.put("questionOptions", option);
                map2.put("questionOptions", option);
            }else if(trainingType==5){
                optionmap.put("type","image");
                optionmap.put("value",ip+"/fileview/uploads/20191113/87ae17f0e97b55199757d442e59071d2.png");
                option.add(optionmap);
                option.add(optionmap);
                option.add(optionmap);
                map.put("questionOptions", option);
                map.put("answerKey",1);
                map1.put("questionOptions", option);
                map2.put("questionOptions", option);
            }else if(trainingType==1){
                map.put("questionOptions", null);
                map1.put("questionOptions", null);
                map2.put("questionOptions", null);
            }else if(trainingType==4){
                optionmap.put("type","text");
                optionmap.put("value","选项1");
                optionmap1.put("type","text");
                optionmap1.put("value","选项2");
                optionmap2.put("type","text");
                optionmap2.put("value","选项3");
                optionmap3.put("type","text");
                optionmap3.put("value","选项4");
                option.add(optionmap);
                option.add(optionmap1);
                option.add(optionmap2);
                option.add(optionmap3);
                map.put("questionOptions", option);
                map.put("answerKey",3);
                map1.put("questionOptions", option);
                map2.put("questionOptions", option);
            }else if(trainingType==7){
                optionmap.put("type","text");
                optionmap.put("value","多选1");
                optionmap1.put("type","text");
                optionmap1.put("value","多选2");
                optionmap2.put("type","text");
                optionmap2.put("value","多选3");
                optionmap3.put("type","text");
                optionmap3.put("value","多选4");
                option.add(optionmap);
                option.add(optionmap1);
                option.add(optionmap2);
                option.add(optionmap3);
                map.put("answerKey",2);
                map.put("questionOptions", option);
                map1.put("questionOptions", option);
                map2.put("questionOptions", option);
            }else if(trainingType==8){
                optionmap.put("type","image");
                optionmap.put("value",ip+"/fileview/uploads/20191113/87ae17f0e97b55199757d442e59071d2.png");
                optionmap1.put("type","image");
                optionmap1.put("value",ip+"/fileview/uploads/20191113/87ae17f0e97b55199757d442e59071d2.png");
                optionmap2.put("type","image");
                optionmap2.put("value",ip+"/fileview/uploads/20191113/87ae17f0e97b55199757d442e59071d2.png");
                optionmap3.put("type","image");
                optionmap3.put("value",ip+"/fileview/uploads/20191113/87ae17f0e97b55199757d442e59071d2.png");
                option.add(optionmap);
                option.add(optionmap1);
                option.add(optionmap2);
                option.add(optionmap3);
                map.put("questionOptions", option);
                map.put("answerKey",1);
                map1.put("questionOptions", option);
                map2.put("questionOptions", option);
            }else if(trainingType==9){
                optionmap.put("type","video");
                optionmap.put("value",ip+"/fileview/uploads/20191113/f20a05e6c6ed3f88bfc5811eabeabeac.mp4");
                optionmap1.put("type","video");
                optionmap1.put("value",ip+"/fileview/uploads/20191113/f20a05e6c6ed3f88bfc5811eabeabeac.mp4");
                optionmap2.put("type","video");
                optionmap2.put("value",ip+"/fileview/uploads/20191113/f20a05e6c6ed3f88bfc5811eabeabeac.mp4");
                optionmap3.put("type","video");
                optionmap3.put("value",ip+"/fileview/uploads/20191113/f20a05e6c6ed3f88bfc5811eabeabeac.mp4");
                option.add(optionmap);
                option.add(optionmap1);
                option.add(optionmap2);
                option.add(optionmap3);
                map.put("questionOptions", option);
                map.put("answerKey",4);
                map1.put("questionOptions", option);
                map2.put("questionOptions", option);
            }else if(trainingType==18){
                optionmap.put("type","txt");
                optionmap.put("value",ip+"/fileview/uploads/20191113/5ca7cb87655464d720f92db93de31470.txt");

                option.add(optionmap);
                map.put("questionOptions", option);
                map.put("answerKey",1);
                map1.put("questionOptions", option);
                map2.put("questionOptions", option);
            }else if(trainingType==19){
                option.add(ip+"/fileview/uploads/20191113/d7821b8fcf30518b9d9a8f6e2b90415d.mp4");
                option.add(ip+"/fileview/uploads/20191113/905fd746bdca5e76be53c3cb5d054bd2.mp3");
                option.add(ip+"/fileview/uploads/20191113/f3b25701fe362ec84616a93a45ce9998.lrc");
                map.put("questionOptions", option);
                map.put("answerKey",4);
                map1.put("questionOptions", option);
                map2.put("questionOptions", option);
            }else if(trainingType==20){
                option.add(ip+"/fileview/uploads/20191113/f20a05e6c6ed3f88bfc5811eabeabeac.mp4");
                option.add(ip+"/fileview/uploads/20191113/dbb626c216d4f65f00d4519b24efdb54.mp3");
                option.add(ip+"/fileview/uploads/20191113/0307154ca8a4696ad8da4d246a227414.mp3");
                option.add(ip+"/fileview/uploads/20191113/905fd746bdca5e76be53c3cb5d054bd2.mp3");
                option.add(ip+"/fileview/uploads/20191113/f3b25701fe362ec84616a93a45ce9998.lrc");
                option.add(ip+"/fileview/uploads/20191113/f3b25701fe362ec84616a93a45ce9998.lrc");
                map.put("questionOptions", option);
                map.put("answerKey",2);
                map1.put("questionOptions", option);
                map2.put("questionOptions", option);
            }else{
                return Result.getSuccessResultData(ErrorDict.DATAERROR,null,null);
            }
            map.put("answerType",chapterId);

            list.add(map);

            return Result.getSuccessResultData(ErrorDict.SUCCESS,list,null);
        }catch (Exception e){
            e.printStackTrace();
            logger.error("获取特训课程作业接口错误"+e);
            return Result.getErrorResult(ErrorDict.ERROR);
        }
    }

    @Override
    public Result getChapterOneTaskList(HttpServletRequest request) {
        try{
            request.setCharacterEncoding("UTF-8");
            String strRequest= RequestJson.parsePostJson(request);
            if(StringUtils.isNull(strRequest)){
                return Result.getResult(0, ErrorDict.DATAERROR);
            }
            JSONObject json=JSONObject.parseObject(strRequest);
            Integer userId=json.getInteger("User_id");
            logger.info("获取作业列表:"+json);

            String token=json.getString("Token");

            if(!isToken(token,userId)){
                return Result.getSuccessResult(ErrorDict.TOKENERROR);
            }

            List<Object> list=new ArrayList<>();

            Map<String,Object> map=new HashMap<>();
            map.put("teacherjob_id",1);
            map.put("quesstion","作业1");
            map.put("status",-1);

            Map<String,Object> map1=new HashMap<>();
            map1.put("teacherjob_id",2);
            map1.put("quesstion","作业2");
            map1.put("status",-1);

            Map<String,Object> map2=new HashMap<>();
            map2.put("teacherjob_id",3);
            map2.put("quesstion","作业3");
            map2.put("status",0);

            Map<String,Object> map3=new HashMap<>();
            map3.put("teacherjob_id",4);
            map3.put("quesstion","作业4");
            map3.put("status",0);

            list.add(map);
            list.add(map1);
            list.add(map2);
            list.add(map3);

            return Result.getSuccessResultData(ErrorDict.SUCCESS,list,null);
        }catch (Exception e){
            e.printStackTrace();
            logger.error("获取作业列表错误");
            return Result.getErrorResult(ErrorDict.ERROR);
        }
    }

    @Override
    public Result getSpecialTraining(HttpServletRequest request) {
        try{
            request.setCharacterEncoding("UTF-8");
            String strRequest= RequestJson.parsePostJson(request);
            if(StringUtils.isNull(strRequest)){
                return Result.getResult(0, ErrorDict.DATAERROR);
            }
            JSONObject json=JSONObject.parseObject(strRequest);
            Integer userId=json.getInteger("User_id");
            logger.info("获取特训课程接口userId="+userId);

            String token=json.getString("Token");

            if(!isToken(token,userId)){
                return Result.getSuccessResult(ErrorDict.TOKENERROR);
            }

            Map<String,Object> map=new HashMap<>();

            SpecialTraining st=new SpecialTraining();
            st.setDetailId(1);
            st.setName("课程1");
            st.setType(1);
            st.setPic("http://goss.cfp.cn/creative/vcg/800/new/32a674c435744dbd90f66342010a05b0.jpg?x-oss-process=image/format,jpg/interlace,1");
            st.setLearnNum("45");
            st.setTime("50");
            st.setFile("http://goss.cfp.cn/creative/vcg/800/new/32a674c435744dbd90f66342010a05b0.jpg?x-oss-process=image/format,jpg/interlace,1");
            st.setDesc("课程描述描述描述描述描述描述描述描述描述描述描述描述描述描述描述描述1");

            SpecialTraining st1=new SpecialTraining();
            st1.setDetailId(2);
            st1.setName("课程2");
            st1.setType(2);
            st1.setPic("http://goss3.cfp.cn/creative/vcg/800/version23/VCG41498809149.jpg?x-oss-process=image/format,jpg/interlace,1");
            st1.setLearnNum("35");
            st1.setTime("40");
            st1.setFile("http://goss3.cfp.cn/creative/vcg/800/version23/VCG41498809149.jpg?x-oss-process=image/format,jpg/interlace,1");
            st1.setDesc("课程描述描述描述描述描述描述描述描述描述描述描述描述描述描述描述描述2");

            SpecialTraining st2=new SpecialTraining();
            st2.setDetailId(3);
            st2.setName("课程3");
            st2.setType(3);
            st2.setPic("http://goss.cfp.cn/creative/vcg/800/version23/VCG41471562191.jpg?x-oss-process=image/format,jpg/interlace,1");
            st2.setLearnNum("55");
            st2.setTime("40");
            st2.setFile("http://goss.cfp.cn/creative/vcg/800/version23/VCG41471562191.jpg?x-oss-process=image/format,jpg/interlace,1");
            st2.setDesc("课程描述描述描述描述描述描述描述描述描述描述描述描述描述描述描述描述3");

            map.put("userType",0);
            map.put("learnedTime","25");
            map.put("surplusTime","35");

            List<SpecialTraining> list=new ArrayList<>();
            list.add(st);
            list.add(st1);
            list.add(st2);
            return Result.getSuccessResultData(ErrorDict.SUCCESS,list,map);
        }catch (Exception e){
            e.printStackTrace();
            logger.error("特训课程接口错误"+e);
            return Result.getErrorResult(ErrorDict.ERROR);
        }
    }

    @Override
    public Result GetSpecialTrainingDetail(HttpServletRequest request) {
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

            Integer type=json.getInteger("Type");
            Integer detailId=json.getInteger("DetailId");

            Map<String,Object> map=new HashMap<>();
            map.put("title","标题标题标题标题标题标题标题标题标题标题"+detailId);
            map.put("pic","http://goss.cfp.cn/creative/vcg/800/version23/VCG41471562191.jpg?x-oss-process=image/format,jpg/interlace,1");
            map.put("desc","课程介绍课程介绍课程介绍课程介绍课程介绍课程介绍课程介绍课程介绍课程介绍课程介绍课程介绍课程介绍课程介绍课程介绍课程介绍课程介绍课程介绍课程介绍课程介绍课程介绍课程介绍课程介绍课程介绍课程介绍课程介绍"+detailId);
            map.put("file","课程介绍");
            return Result.getSuccessResultData(ErrorDict.SUCCESS,null,map);
        }catch (Exception e){
            e.printStackTrace();
            logger.error("获取特训课程接口错误"+e);
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
