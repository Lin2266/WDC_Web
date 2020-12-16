package com.test.commons.util.internal;

import org.springframework.cglib.proxy.Enhancer;

/**
 * class 的輔助工具.
 */
public class SpringClassUtil {
    public static boolean isEnhanced(Class<?> clazz) {
        return Enhancer.isEnhanced(clazz);
    }
    
    /**
     * 取得未被 cglib 加強處理過的 class.<br/>
     * <b>Note</b>:取自 http://viewvc.internet2.edu/viewvc.py/grouper/src/grouper/edu/internet2/middleware/grouper/util/GrouperUtil.java?revision=1.11&root=I2MI
     * @param clazz
     * @return
     */
    public static Class<?> getUnenhancedClass(Class<?> clazz) {
        try {
            while(Enhancer.isEnhanced(clazz))
                clazz = clazz.getSuperclass();
            return clazz;
        } catch(Throwable t) {
            throw new RuntimeException("problem unenhancing " + clazz, t);
        }
    }
    
    /**
     * 取未被 cglib 增強處理過的 class name (null safe).<br/>
     * <b>Note</b>:取自 http://viewvc.internet2.edu/viewvc.py/grouper/src/grouper/edu/internet2/middleware/grouper/util/GrouperUtil.java?revision=1.11&root=I2MI
     * @param object
     * @return
     */
    public static String getUnenhancedClassName(Object object) {
        return (object == null) ? null : getUnenhancedClass(object.getClass()).getName();
    }
}
