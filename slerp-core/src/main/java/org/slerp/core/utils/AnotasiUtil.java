/**
 * 
 */
package org.slerp.core.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.slerp.core.CoreException;

/**
 * @author Rifky Aditya Bastara
 *
 */
public class AnotasiUtil {

	public static Object annotatedRuntime(Class<? extends Annotation> annotatedClasses, Class<?> kelasAwal) {
		Annotation anotasi = null;
		Object kelasObject;
		try {
			kelasObject = kelasAwal.newInstance();
			anotasi = kelasObject.getClass().getAnnotation(annotatedClasses);
			if (kelasObject.getClass().isAnnotationPresent(annotatedClasses)) {
				if (anotasi != null)
					return anotasi;
			}

		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Annotation annotatedMethod(Class<? extends Annotation> kelasAnotasi, Class<?> kelas) {
		Annotation anotasi = null;
		Object object = null;
		try {
			object = kelas.newInstance();
			Method[] methods = object.getClass().getMethods();
			if (methods == null)
				throw new CoreException("Tidak terdapat method");
			for (Method method : methods) {
				anotasi = method.getAnnotation(kelasAnotasi);
				if (anotasi == null)
					throw new CoreException("Anotasi tidak boleh null");
				return anotasi;
			}
		} catch (Exception e) {
			throw new CoreException("Gagal membuat deklarasi anotasi!!", e);
		}
		return null;
	}
}
