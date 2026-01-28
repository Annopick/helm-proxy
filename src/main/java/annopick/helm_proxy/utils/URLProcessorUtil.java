package annopick.helm_proxy.utils;

import annopick.helm_proxy.entity.HelmRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class URLProcessorUtil {
    public static void processUrls(Map<String, Object> versionData, HelmRepository repo) {
        // 获取urls数组
        ArrayList<String> urls = (ArrayList<String>) versionData.get("urls");

        if (urls == null) {
            return; // 如果urls为空，直接返回
        }

        // 创建新的urls数组用于存储处理后的结果

        List<String> newUrls = urls.stream()
                .map(url -> isCompleteUrl(url) ? url : getCompleteUrl(url, repo.getUrl()) )
                .collect(Collectors.toList());


        // 将修改后的新urls放回versionData的"urls"里
        versionData.put("urls", new ArrayList<>(newUrls));
    }

    /**
     * 判断是否为完整的URL（HTTP或OCI协议）
     */
    private static boolean isCompleteUrl(String url) {
        if (url == null) {
            return false;
        }
        return url.toLowerCase().contains("://");
    }

    private static String getCompleteUrl(String url, String prefix) {
        if (!prefix.endsWith("/") && !url.startsWith("/")) {
            return prefix + "/" + url;
        } else if (prefix.endsWith("/") && url.startsWith("/")) {
            return prefix + url.substring(1); // 去除url开头的斜杠避免重复
        } else {
            return prefix + url;
        }
    }
}
