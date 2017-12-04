package test.jedis;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import test.jedis.bean.Club;
import test.jedis.serializer.ProtostuffSerializer;

import java.util.Date;
import java.util.List;

/**
 * Created by author on 17-12-3.
 */
public class JedisTest {
    public static void main(String[] args) {
        //jedisTestWithPool();
//        jedisTestWithPipeline();
        jedisTestWithLua();

    }


    private static void jedisTestWithLua() {
        Jedis jedis = new Jedis("127.0.0.1");
        String key = "hello";
        String script = "return redis.call('get',KEYS[1])";
        Object result = jedis.eval(script, 1, key);
        System.out.println(result);
    }

    private static void jedisTestWithPipeline() {
        Jedis jedis = new Jedis("127.0.0.1");
        Pipeline pipeline = jedis.pipelined();
        pipeline.set("hello", "world");
        pipeline.incr("counter");
        List<Object> resultList = pipeline.syncAndReturnAll();
        for (Object o : resultList) {
            System.out.println(o);
        }
    }

    private static void jedisTestWithPool() {
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        JedisPool jedisPool = new JedisPool(config, "127.0.0.1", 6379);

        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            System.out.println(jedis.get("hello"));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }

    }

    private static void jedisTestWithoutPool() {
        /**
         * 直连
         */
        Jedis jedis = new Jedis("127.0.0.1", 6379);
        /*jedis.set("hello", "world");
        String value = jedis.get("hello");
        System.out.println(value);
        */
        ProtostuffSerializer serializer = new ProtostuffSerializer();
        String key = "club:1";
        Club club = new Club(1, "AC", "米兰", new Date(), 1);
        byte[] clubBytes = serializer.serialize(club);
        jedis.set(key.getBytes(), clubBytes);

        byte[] resultBytes = jedis.get(key.getBytes());
        Club resultClub = serializer.deserialize(resultBytes);
        System.out.println(resultClub);
    }
}
