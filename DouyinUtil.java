import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jinmgr.entity.MediaModule;
import com.jinmgr.entity.MixInfo;
import com.jinmgr.utils.Constants;
import com.jinmgr.utils.OkHttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 抓取抖音视频信息工具类
 * <p>
 * 视频集合请求地址:https://www.douyin.com/aweme/v1/web/mix/aweme/
 *
 * @author LiuChenghui 2024/03/13 10:27:00
 * @version 1.0
 */
public class DouyinUtil {
    private static Logger logger = LoggerFactory.getLogger(DouyinUtil.class);
    private static final String USER_MIX = "https://www.douyin.com/aweme/v1/web/mix/aweme/";

    /**
     * 根据集合ID获取集合列表
     *
     * @param mixId
     * @param all   是否获取所有视频列表数据
     * @return
     */
    public static MixInfo getUserMixList(String mixId) {
        MixInfo mixInfo = null;
        try {
            List<MediaModule> list = new ArrayList<>();
            int startIndex = 0;
            int pageSize = 10;
            int fcount = 0;   //失败次数
            while (true) {
                String param = "mix_id=" + mixId + "&cursor=" + startIndex + "&count=" + pageSize + "&device_platform=webapp&aid=6383";
                Map<String, String> headers = new HashMap<>();
                try {
                    String res = OkHttpUtil.doGet(USER_MIX, param, Constants.DouyinUrl.getDouyinHearder());
                    if (res != null && res.length() > 0) {
                        JSONObject data = JSONObject.parseObject(res);
                        JSONArray dataArray = data.getJSONArray("aweme_list");
                        if (dataArray == null) return null;  //无效集合ID
                        logger.info("本次请求返回:" + dataArray.size() + "条数据");
                        //更新下标
                        startIndex = data.getInteger("cursor");
                        fcount = 0;
                        for (int i = 0; i < dataArray.size(); i++) {
                            JSONObject object = dataArray.getJSONObject(i);
                            //集合信息
                            if (mixInfo == null) {
                                mixInfo = new MixInfo();
                                //集合ID
                                mixInfo.setMixId(object.getJSONObject("mix_info").getString("mix_id"));
                                //集合名称
                                mixInfo.setMixName(object.getJSONObject("mix_info").getString("mix_name"));
                                //集合预览图,地址有有效期限制
                                mixInfo.setImageUrl(object.getJSONObject("mix_info").getJSONObject("cover_url").getJSONArray("url_list").get(0).toString());
                                //作者
                                mixInfo.setAuthName(object.getJSONObject("author").getString("nickname"));
                            }
                            //当前第几集
                            Integer curIndex = object.getJSONObject("mix_info").getJSONObject("statis").getInteger("current_episode");
                            //最新集数
                            Integer lastIndex = object.getJSONObject("mix_info").getJSONObject("statis").getInteger("updated_to_episode");
                            //视频ID
                            String id = object.getString("aweme_id");
                            //视频标签
                            String tag = object.getString("caption");
                            //标题
                            String title = object.getString("desc").replaceAll(tag, "").replaceAll("[\\\\/:\\*\\?\"<>\\|_。]", "").replaceAll("#([\\S]+)", "").trim();
                            //创建时间
                            String createtime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date(object.getLong("create_time") * 1000));
                            //图片
                            String image = object.getJSONObject("video").getJSONObject("origin_cover").getJSONArray("url_list").get(0).toString();
                            //播放地址
                            String playUrl = object.getJSONObject("video").getJSONObject("play_addr").getJSONArray("url_list").get(0).toString();
                            MediaModule module = new MediaModule();
                            module.setMediaUrl(playUrl);
                            module.setDateTime(createtime);
                            module.setTitle("第" + curIndex + "集_" + title);
                            module.setPlayIndex(curIndex);
                            module.setImageUrl(image);
                            list.add(module);
                        }
                        //退出条件
                        if (data.getInteger("has_more") == 0 || !data.getBoolean("has_more")) {
                            logger.info("集合:" + mixId + ",获取完成,共" + list.size() + "条");
                            if (mixInfo != null) {
                                mixInfo.setMediaModuleList(list);
                            }
                            break;
                        }
                    } else {
                        fcount++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    fcount++;
                }
                if (fcount > 2) {
                    logger.info("url:" + Constants.DouyinUrl.USER_MIX + "?" + param + ",连续失败3次,退出获取数据");
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("获取抖音集合数据异常:" + e.getMessage(), e);
        }
        return mixInfo;
    }
}
