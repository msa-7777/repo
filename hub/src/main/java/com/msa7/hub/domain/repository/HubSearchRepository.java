package com.msa7.hub.domain.repository;

import com.msa7.hub.domain.model.Hub;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HubSearchRepository {

    Page<Hub> search(String name, String address, Boolean isCentral, Pageable pageable);
}
