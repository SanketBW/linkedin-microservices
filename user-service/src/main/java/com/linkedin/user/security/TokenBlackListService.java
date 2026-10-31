package com.linkedin.user.security;

import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlackListService {
    private final Set<String> blacklist = ConcurrentHashMap.newKeySet(); // it is thread safe

    public void add(String token) {             // AuthServiceImpl
        blacklist.add(token);
    }

    public boolean contains(String token) {         //JwtAuthFilter
        return blacklist.contains(token);
    }
}

