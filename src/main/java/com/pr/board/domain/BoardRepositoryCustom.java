package com.pr.board.domain;

import com.pr.config.SearchCondition;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.pr.board.domain.QBoard.board;

@RequiredArgsConstructor
@Repository
public class BoardRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    public Page<Board> findAllBySearchCondition(Pageable pageable, SearchCondition searchCondition) {
        JPAQuery<Board> query = queryFactory.selectFrom(board)
                .where(searchKeywords(searchCondition.getSk(), searchCondition.getSv()));

        long total = query.stream().count();   //여기서 전체 카운트 후 아래에서 조건작업

        List<Board> results = query
                .where(searchKeywords(searchCondition.getSk(), searchCondition.getSv()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(board.id.desc())
                .fetch();

        return new PageImpl<>(results, pageable, total);
    }

    private BooleanExpression searchKeywords(String sk, String sv) {
        if("nickName".equals(sk)) {
            if(StringUtils.hasLength(sv)) {
                return board.nickName.contains(sv);
            }
        } else if ("title".equals(sk)) {
            if(StringUtils.hasLength(sv)) {
                return board.title.contains(sv);
            }
        } else if ("content".equals(sk)) {
            if(StringUtils.hasLength(sv)) {
                return board.content.contains(sv);
            }
        }

        return null;
    }
}