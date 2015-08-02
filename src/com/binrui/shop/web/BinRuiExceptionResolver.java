package com.binrui.shop.web;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;
/**
 * 通用异常处理
 * @author lijinfeng
 * @version 1.0
 * @date 2015-08-02 20：29
 * @title 通用异常处理
 */
public class BinRuiExceptionResolver extends SimpleMappingExceptionResolver {

	@Override
	protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response,
			Object handler, Exception ex) {
		// Expose ModelAndView for chosen error view.
		String viewName = determineViewName(ex, request);
		if (viewName != null) {
			// Apply HTTP status code for error views, if specified.
			// Only apply it if we're processing a top-level request.
			Integer statusCode = determineStatusCode(request, viewName);
			if (statusCode != null) {
				applyStatusCodeIfPossible(request, response, statusCode);
			}
			request.setAttribute("exceptionMessage", ex.getMessage());
			return getModelAndView(viewName, ex, request);
		}else {
			return null;
		}
	}
}