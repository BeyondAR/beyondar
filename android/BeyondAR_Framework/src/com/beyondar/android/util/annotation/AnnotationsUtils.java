package com.beyondar.android.util.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class AnnotationsUtils {

	/**
	 * Check if the object class has the annotation in the specified method
	 * 
	 * @param object
	 * @param methodName
	 * @return true if the object class has the annotation {@link OnUiThread}
	 */
	public static boolean hasUiAnnotation(Object object, String methodName) {
		return hasAnnotation(OnUiThread.class, object, methodName);
	}
	
	public static boolean hasAnnotation(Class<? extends Annotation> annotation,Object object, String methodName) {
		try {
			Class<? extends Object> c = object.getClass();

			for (Method m : c.getMethods()) {
				if (m.getName().equals(methodName)) {
					if (m.isAnnotationPresent(annotation)) {
						return true;
					}
				}
			}
		} catch (Exception e) {
		}
		return false;
	}
}
