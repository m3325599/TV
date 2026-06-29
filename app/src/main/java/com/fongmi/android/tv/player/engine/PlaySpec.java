package com.fongmi.android.tv.player.engine;

import android.net.Uri;
import android.text.TextUtils;

import androidx.media3.common.MediaMetadata;

import com.fongmi.android.tv.bean.Danmaku;
import com.fongmi.android.tv.bean.Drm;
import com.fongmi.android.tv.bean.Result;
import com.fongmi.android.tv.bean.Sub;
import com.fongmi.android.tv.player.PlayerHelper;
import com.fongmi.android.tv.setting.Setting;
import com.fongmi.android.tv.utils.UrlUtil;
import com.google.common.net.HttpHeaders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlaySpec {

    // 网盘特殊User-Agent
    private static final String UA_BAIDU_PAN = "pan.baidu.com";
    private static final String UA_ALIYUN_PAN = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36";

    private Map<String, String> headers;
    private List<Danmaku> danmakus;
    private MediaMetadata metadata;
    private List<Sub> subs;
    private String format;
    private String key;
    private String url;
    private Drm drm;

    public static PlaySpec from(String key, String url, Map<String, String> headers, MediaMetadata metadata) {
        return new PlaySpec(key, url, headers, null, null, null, null, metadata);
    }

    public static PlaySpec from(Result result, String key, MediaMetadata metadata) {
        return new PlaySpec(key, result.getRealUrl(), result.getHeader(), result.getFormat(), result.getDrm(), result.getSubs(), result.getDanmaku(), metadata);
    }

    public static PlaySpec fromParse(Result result, String key, MediaMetadata metadata) {
        return new PlaySpec(key, null, null, result.getFormat(), result.getDrm(), result.getSubs(), result.getDanmaku(), metadata);
    }

    private PlaySpec(String key, String url, Map<String, String> headers, String format, Drm drm, List<Sub> subs, List<Danmaku> danmakus, MediaMetadata metadata) {
        this.key = key;
        this.url = url;
        this.drm = drm;
        this.subs = subs;
        this.format = format;
        this.headers = headers;
        this.danmakus = danmakus;
        this.metadata = metadata;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Uri getUri() {
        return UrlUtil.uri(url);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Drm getDrm() {
        return drm;
    }

    public List<Sub> getSubs() {
        return subs;
    }

    public List<Danmaku> getDanmakus() {
        return danmakus;
    }

    public MediaMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(MediaMetadata metadata) {
        this.metadata = metadata;
    }

    /**
     * 检查并设置必要的请求头
     * 包括User-Agent和网盘特殊headers
     */
    public PlaySpec checkUa() {
        if (headers == null) headers = new HashMap<>();
        // 检查是否需要设置网盘特殊User-Agent
        String panUa = getPanUserAgent(url);
        if (panUa != null) {
            // 如果URL来自网盘，强制使用网盘UA
            headers.put(HttpHeaders.USER_AGENT, panUa);
        } else if (headers.keySet().stream().noneMatch(HttpHeaders.USER_AGENT::equalsIgnoreCase)) {
            // 否则使用默认UA
            headers.put(HttpHeaders.USER_AGENT, Setting.getUa().isEmpty() ? PlayerHelper.getDefaultUa() : Setting.getUa());
        }
        return this;
    }

    /**
     * 根据URL判断是否需要设置特殊的网盘User-Agent
     * @param url 播放URL
     * @return 需要的User-Agent，如果不需要则返回null
     */
    private String getPanUserAgent(String url) {
        if (TextUtils.isEmpty(url)) return null;
        String lowerUrl = url.toLowerCase();

        // 百度网盘 - 需要User-Agent包含pan.baidu.com
        if (lowerUrl.contains("baidu.com") || lowerUrl.contains("baidupcs.com")) {
            return UA_BAIDU_PAN;
        }

        // 阿里云盘 - 需要浏览器UA
        if (lowerUrl.contains("aliyundrive.com") || lowerUrl.contains("aliyun.com") ||
            lowerUrl.contains("alicloud.com") || lowerUrl.contains("aliyuncs.com")) {
            return UA_ALIYUN_PAN;
        }

        return null;
    }

    public void setSub(Sub sub) {
        if (subs == null) subs = new ArrayList<>();
        if (sub != null && !subs.contains(sub)) subs.add(0, sub);
    }

    public void setDanmaku(Danmaku item) {
        if (danmakus == null) danmakus = new ArrayList<>();
        if (!item.isEmpty() && !danmakus.contains(item)) danmakus.add(0, item);
        danmakus.forEach(danmaku -> danmaku.setSelected(danmaku.getUrl().equals(item.getUrl())));
    }

    public void addDanmaku(Danmaku item) {
        if (danmakus == null) danmakus = new ArrayList<>();
        if (!item.isEmpty() && !danmakus.contains(item)) danmakus.add(item);
    }
}
