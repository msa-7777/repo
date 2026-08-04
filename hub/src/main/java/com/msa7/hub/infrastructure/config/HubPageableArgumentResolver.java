package com.msa7.hub.infrastructure.config;

import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Set;

@Component
public class HubPageableArgumentResolver extends PageableHandlerMethodArgumentResolver  {


    public static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "createdAt", "updatedAt");
    private static final Set<Integer> ALLOWED_SIZE = Set.of(10, 30, 50);
    private static final int DEFAULT_PAGE_SIZE =10;

    @Override
    public Pageable resolveArgument(MethodParameter methodParameter, @Nullable ModelAndViewContainer mavContainer,
                                    NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) {

        Pageable pageable = super.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory);

        int pageSize = pageable.getPageSize();
        int pageNumber = pageable.getPageNumber();
        Sort sort = pageable.getSort();

        if (!ALLOWED_SIZE.contains(pageSize)) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        if (!sort.isSorted()) {
            sort = DEFAULT_SORT;
        }

        return PageRequest.of(pageNumber, pageSize, sort);
    }
}
