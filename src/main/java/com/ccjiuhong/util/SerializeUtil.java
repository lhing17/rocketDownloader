package com.ccjiuhong.util;

import lombok.extern.slf4j.Slf4j;
import org.nustaq.serialization.FSTConfiguration;

import java.io.*;

/**
 * @author G. Seinfeld
 * @since 2019/12/25
 */
@Slf4j
public final class SerializeUtil {
    private static ThreadLocal<FSTConfiguration> confs = ThreadLocal.withInitial(FSTConfiguration::createDefaultConfiguration);

    private SerializeUtil() {
    }

    public static <T> void serialize(T t, File file) {
        try {
            byte[] bytes = serialize(t);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bytes);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public static <T> T deserialize(File file, Class<T> c) {
        try {
            FileInputStream fis = new FileInputStream(file);
            // TODO 如果文件长度大于整数最大值，怎么处理？
            byte[] bytes = new byte[(int) file.length()];
            fis.read(bytes);
            return deserialize(bytes, c);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private static FSTConfiguration getFST() {
        return confs.get();
    }

    public static <T> byte[] serialize(T t) {
        return getFST().asByteArray(t);
    }


    @SuppressWarnings("unchecked")
    public static <T> T deserialize(byte[] bytes, Class<T> c) {
        return (T) getFST().asObject(bytes);
    }

}
