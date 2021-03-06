package cn.wuxia.common.cached.memcached;

import cn.wuxia.common.cached.CacheClient;
import cn.wuxia.common.util.ListUtil;
import cn.wuxia.common.util.StringUtil;
import com.google.common.collect.Lists;
import net.rubyeye.xmemcached.*;
import net.rubyeye.xmemcached.utils.AddrUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * [ticket id]
 * Description of the class
 *
 * @author songlin.li
 * @ Version : V<Ver.No> <2012年8月30日>
 */
public class XMemcachedClient implements CacheClient {
    private static Logger logger = LoggerFactory.getLogger(XMemcachedClient.class);

    private MemcachedClient memcachedClient;

    private int expiredTime = 0;

    private String namespace;

    /**
     * 从缓存中获取值
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key, String namespace) {
        try {
            MemcachedUtils.validateKey(key);
            if (StringUtil.isNotBlank(namespace)) {
                memcachedClient.beginWithNamespace(namespace);
            }
            return (T) memcachedClient.get(key);
        } catch (RuntimeException e) {
            logger.warn("Get from memcached server fail,key is " + key, e);
            return null;
        } catch (TimeoutException e) {
            logger.warn("Get from memcached server fail,key is " + key, e);
            return null;
        } catch (InterruptedException e) {
            logger.warn("Get from memcached server fail,key is " + key, e);
            return null;
        } catch (Exception e) {
            logger.warn("Get from memcached server fail,key is " + key, e);
            return null;
        }finally {
            if (StringUtil.isNotBlank(namespace)) {
                memcachedClient.endWithNamespace();
            }
        }
    }

    /**
     * 添加到缓存，不允许重复key，如重复则报错
     *
     * @param key
     * @param value
     * @param expiredTime 缓存失效时间单位秒
     * @param namespace
     * @return
     */
    @Override
    public void add(String key, Object value, int expiredTime, String namespace) {
        if (value == null) {
            return;
        }
        boolean isadd = false;
        try {
            MemcachedUtils.validateKey(key);
            if (StringUtil.isNotBlank(namespace)) {
                memcachedClient.beginWithNamespace(namespace);
            }
            isadd = memcachedClient.add(key, expiredTime, value);
        } catch (Exception e) {
            logger.warn("add from memcached server fail,key is " + key, e);
        }finally {
            if (StringUtil.isNotBlank(namespace)) {
                memcachedClient.endWithNamespace();
            }
        }
        if (!isadd) {
            logger.warn("添加{}缓存失败。", key);
        }
    }

    @Override
    public void add(String key, Object value, String namespace) {
        add(key, value, expiredTime, namespace);
    }

    /**
     * 添加到缓存，不允许重复key，如重复则报错
     */
    @Override
    public void add(String key, Object value) {
        add(key, value, expiredTime);
    }

    /**
     * 增加一个缓存，如key存在则替换原来的值
     *
     * @param key
     * @param value
     * @param expiredTime 缓存失效时间单位秒
     * @param namespace
     * @return
     */
    @Override
    public void set(String key, Object value, int expiredTime, String namespace) {
        if (value == null) {
            logger.warn("key[{}] --> value can't be null");
            return;
        }
        boolean isset = false;
        try {
            MemcachedUtils.validateKey(key);
            if (StringUtil.isNotBlank(namespace)) {
                memcachedClient.beginWithNamespace(namespace);
            }
            isset = memcachedClient.set(key, expiredTime, value);
        } catch (Exception e) {
            logger.warn("Set from memcached server fail,key is " + key, e);
        }finally {
            if (StringUtil.isNotBlank(namespace)) {
                memcachedClient.endWithNamespace();
            }
        }
        if (!isset) {
            logger.warn("添加{}缓存失败。", key);
        }
    }

    @Override
    public void set(String key, Object value, String namespace) {
        set(key, value, expiredTime, namespace);
    }

    /**
     * 增加一个缓存，如key存在则替换原来的值,返回true
     */
    @Override
    public void set(String key, Object value) {
        set(key, value, expiredTime);
    }

    /**
     * 替换一个缓存，如果缓存key存在则替换并返回true，如果不存在则不替换并返回false
     *
     * @param key
     * @param value
     * @param expiredTime
     * @param namespace
     * @return
     */
    @Override
    public void replace(String key, Object value, int expiredTime, String namespace) {
        if (value == null) {
            return;
        }
        boolean isreplace = false;
        try {
            MemcachedUtils.validateKey(key);
            if (StringUtil.isNotBlank(namespace)) {
                memcachedClient.beginWithNamespace(namespace);
            }
            isreplace = memcachedClient.replace(key, expiredTime, value);
        } catch (Exception e) {
            logger.warn("replace from memcached server fail,key is " + key, e);
        }finally {
            if (StringUtil.isNotBlank(namespace)) {
                memcachedClient.endWithNamespace();
            }
        }
        if (!isreplace) {
            logger.warn("替换{}缓存失败。", key);
        }
    }

    @Override
    public void replace(String key, Object value, String namespace) {
        replace(key, value, expiredTime, namespace);
    }

    /**
     * 替换一个缓存，如果缓存key存在则替换并返回true，如果不存在则不替换并返回false
     */
    @Override
    public void replace(String key, Object value) {
        replace(key, value, expiredTime);

    }

    /**
     * 删除一个缓存数据
     */
    @Override
    public void delete(String key, String namespace) {
        boolean isdelete = false;
        try {
            MemcachedUtils.validateKey(key);
            if (StringUtil.isNotBlank(namespace)) {
                memcachedClient.beginWithNamespace(namespace);
            }
            isdelete = memcachedClient.delete(key);
        } catch (Exception e) {
            logger.warn("Delete from memcached server fail,key is " + key, e);
        }finally {
            if (StringUtil.isNotBlank(namespace)) {
                memcachedClient.endWithNamespace();
            }
        }
        if (!isdelete) {
            logger.warn("删除{}缓存失败。", key);
        }
    }


    /**
     * Get with the Check and Set methods, the result of the conversion type and
     * shielding exception.
     */
    @SuppressWarnings("unchecked")
    public <T> GetsResponse<T> gets(String key) {
        try {
            return (GetsResponse<T>) memcachedClient.gets(key);
        } catch (Exception e) {
            logger.warn("Gets from memcached server fail,key is" + key, e);
            return null;
        }
    }

    /**
     * Check and Set method.
     */
    public <T> Boolean cas(String key, long casId, Object value) {
        try {
            MemcachedUtils.validateKey(key);
            return memcachedClient.cas(key, 0, new CASOperation<Integer>() {
                @Override
                public int getMaxTries() {
                    return 1;
                }

                @Override
                public Integer getNewValue(long currentCAS, Integer currentValue) {
                    return 2;
                }

            });
        } catch (TimeoutException e) {
            logger.warn("Cas from memcached server fail,key is" + key, e);
            return false;
        } catch (InterruptedException e) {
            logger.warn("Cas from memcached server fail,key is" + key, e);
            return false;
        } catch (Exception e) {
            logger.warn("Cas from memcached server fail,key is" + key, e);
            return false;
        }
    }

    @Override
    public long incr(String key) {
        return incr(key, 1, 0);
    }

    @Override
    public long incr(String key, String namespace) {
        return incr(key, 1, 0, namespace);
    }

    @Override
    public long incr(String key, long by) {
        return incr(key, by, 0);
    }

    /**
     * 自增长
     */
    @Override
    public long incr(String key, long by, long defaultValue, String namespace) {
        try {
            MemcachedUtils.validateKey(key);
            Object v = memcachedClient.get(key);
            if (v == null) {
                memcachedClient.add(key, expiredTime, "" + defaultValue);
                return defaultValue;
            }
            return memcachedClient.incr(key, by);
        } catch (TimeoutException e) {
            logger.warn("incr from memcached server fail,key is " + key, e);
            return defaultValue;
        } catch (InterruptedException e) {
            logger.warn("incr from memcached server fail,key is " + key, e);
            return defaultValue;
        } catch (Exception e) {
            logger.warn("incr from memcached server fail,key is " + key, e);
            return defaultValue;
        }
    }

    @Override
    public long incr(String key, long by, long defaultValue) {
        long v = incr(key, by, defaultValue, namespace);
        return v;
    }

    @Override
    public long decr(String key) {
        return decr(key, 1, 0);
    }

    @Override
    public long decr(String key, String namespace) {
        return decr(key, 1, 0, namespace);
    }

    @Override
    public long decr(String key, long by) {
        return decr(key, by, 0);
    }

    /**
     * 递减
     */
    @Override
    public long decr(String key, long by, long defaultValue, String namespace) {
        try {
            MemcachedUtils.validateKey(key);
            Object v = memcachedClient.get(key);
            if (v == null) {
                memcachedClient.add(key, expiredTime, "" + defaultValue);
                return defaultValue;
            }
            return memcachedClient.decr(key, by);
        } catch (TimeoutException e) {
            logger.warn("Decr from memcached server fail,key is " + key, e);
            return defaultValue;
        } catch (InterruptedException e) {
            logger.warn("Decr from memcached server fail,key is " + key, e);
            return defaultValue;
        } catch (Exception e) {
            logger.warn("Decr from memcached server fail,key is " + key, e);
            return defaultValue;
        }
    }

    @Override
    public long decr(String key, long by, long defaultValue) {
        long v = decr(key, by, defaultValue, namespace);
        return v;
    }

    @Override
    public void flushAll() {
        try {
            if (StringUtil.isNotBlank(namespace)) {
                memcachedClient.invalidateNamespace(namespace);
            } else {
                memcachedClient.flushAll();
            }
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }

    }

    public void flushAll(String server) {
        try {
            memcachedClient.flushAll(AddrUtil.getOneAddress(server));
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }

    }

    /**
     * 逗号隔开
     * @param servers
     */
    public void addServer(String servers) {
        try {
            memcachedClient.addServer(MemcachedUtils.formatServerUrl(servers));
        } catch (IOException e) {
            logger.warn(" Add Server error:" + e.getMessage(), e);
        }
    }

    public void removeServer(String servers) {
        try {
            memcachedClient.removeServer(MemcachedUtils.formatServerUrl(servers));
        } catch (Exception e) {
            logger.warn(" Add Server error:" + e.getMessage(), e);
        }
    }

    @Override
    public boolean containKey(String key, String namespace) {
        if (get(key, namespace) != null) {
            return true;
        }
        return false;
    }

    @Override
    public boolean containKey(String key) {
        if (get(key) != null) {
            return true;
        }
        return false;
    }

    @Override
    public void flushAll(String[] servers) {
        for (String server : servers) {
            flushAll(server);
        }
    }

    /**
     * @param memcachedClient
     */
    public void setMemcachedClient(net.rubyeye.xmemcached.MemcachedClient memcachedClient) {
        this.memcachedClient = memcachedClient;
    }

    public void setExpiredTime(int expiredTime) {
        this.expiredTime = expiredTime;
    }

    public net.rubyeye.xmemcached.MemcachedClient getMemcachedClient() {
        return memcachedClient;
    }

    public int getExpiredTime() {
        return expiredTime;
    }

    @Override
    public void init(String... addrss) {
        if (ArrayUtils.isEmpty(addrss)) {
            throw new MemcachedException("初始化失败，没找到memcached服务端");
        }
        List<String> addrs = ListUtil.arrayToList(addrss);
        List<Integer> weights = Lists.newArrayList();
        for (int i = 0; i < addrs.size(); i++) {
            weights.add(i + 1);
        }
        try {
            // addrs, weights
            MemcachedClientBuilder builder = new XMemcachedClientBuilder(AddrUtil.getAddressMap(StringUtil.join(addrs, " ")));
            memcachedClient = builder.build();
            builder.setFailureMode(true);
        } catch (IOException e1) {
            throw new MemcachedException("初始化失败", e1.getMessage());
        }
    }

    @Override
    public void shutdown() {
        try {
            memcachedClient.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void add(String key, Object value, int expiredTime) {
        add(key, value, expiredTime, namespace);
    }

    @Override
    public void set(String key, Object value, int expiredTime) {
        set(key, value, expiredTime, namespace);

    }

    @Override
    public void replace(String key, Object value, int expiredTime) {
        replace(key, value, expiredTime, namespace);

    }

    @Override
    public <T> T get(String key) {
        T obj = get(key, namespace);
        return obj;
    }

    @Override
    public void delete(String key) {
        delete(key, namespace);
    }

    @Override
    public void flush(String namespace) {
        try {
            memcachedClient.invalidateNamespace(namespace);
        } catch (net.rubyeye.xmemcached.exception.MemcachedException | InterruptedException | TimeoutException e) {
            logger.warn("清除空间【" + namespace + "】失败", e);
        }
    }
}
