package community.api.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Category {
        BACKEND("백엔드"),
        FRONTEND("프론트엔드"),
        DATABASE("데이터베이스"),
        INFRA("인프라"),
        ETC("기타");

        private final String name;
}
