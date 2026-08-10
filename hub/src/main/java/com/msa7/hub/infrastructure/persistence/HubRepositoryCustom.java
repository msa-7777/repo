package com.msa7.hub.infrastructure.persistence;

import com.msa7.hub.domain.model.Hub;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HubRepositoryCustom {

    Page<Hub> search(String name, String address, Boolean isCentral, Pageable pageable);
}
