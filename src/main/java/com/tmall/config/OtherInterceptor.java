package com.tmall.config;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.tmall.entity.Category;
import com.tmall.entity.OrderItem;
import com.tmall.entity.User;
import com.tmall.service.CategoryService;
import com.tmall.service.OrderItemService;

@Component
public class OtherInterceptor implements HandlerInterceptor {
    @Autowired
    CategoryService categoryService;
    @Autowired
    OrderItemService orderItemService;

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
        HttpSession session = httpServletRequest.getSession();
        User user = (User) session.getAttribute("user");
        int cartTotalItemNumber = 0;
        if (null != user) {
            List<OrderItem> items = orderItemService.listUnBuyByUserId(user.getId());
            for (OrderItem item : items) {
                cartTotalItemNumber += item.getNumber();
            }
        }

        if (httpServletRequest.getServletContext().getAttribute("categories_below_search") == null) {
            List<Category> cs = categoryService.list();
            httpServletRequest.getServletContext().setAttribute("categories_below_search", cs);
        }

        session.setAttribute("cartTotalItemNumber", cartTotalItemNumber);

        String contextPath = httpServletRequest.getServletContext().getContextPath();
        httpServletRequest.getServletContext().setAttribute("contextPath", contextPath);
    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
    }
}
