package com.pr.board.controller;

import com.pr.board.dto.BoardDto;
import com.pr.board.model.Header;
import com.pr.board.repository.BoardRepository;
import com.pr.board.service.BoardService;
import com.pr.config.SearchCondition;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin
public class BoardController {

    private final BoardService boardService;

    private final BoardRepository boardRepository;

    private final HttpSession httpSession;

    //게시판 리스트 가져오기
    @RequestMapping(value="/board/list", method=RequestMethod.GET)
    public Header<List<BoardDto>> boardList(@PageableDefault(sort = {"id"}) Pageable pageable, SearchCondition searchCondition) {
        return boardService.getBoardList(pageable, searchCondition);
    }

    //게시글 조회
    @RequestMapping(value="/board/detail/{id}", method=RequestMethod.GET)
    public BoardDto boardDetail(@PathVariable("id") Long id) {
        Optional<Long> nextBoardIdOptional = boardRepository.findNextBoardId(id); // 다음 게시글 id
        Optional<Long> previousBoardIdOptional = boardRepository.findPreviousBoardId(id); // 이전 게시글 id

        // Optional에서 값 추출
        Long nextBoardId = nextBoardIdOptional.orElse(null);
        Long previousBoardId = previousBoardIdOptional.orElse(null);

        // 로그 출력
        System.out.println(nextBoardId + "@@@@@@@" + previousBoardId);

        // BoardDto 반환
        BoardDto boardDto = boardService.getBoardDetail(id);
        boardDto.setNextBoardId(nextBoardId);
        boardDto.setPreviousBoardId(previousBoardId);

        return boardService.getBoardDetail(id);
    }

    //게시글 작성
    @RequestMapping(value="/board/create", method=RequestMethod.POST)
    public BoardDto createBoard(@RequestBody BoardDto boardDto) {
        return boardService.createBoard(boardDto);
    }

    //게시글 수정
    @RequestMapping(value="/board/update", method=RequestMethod.PUT)
    public BoardDto updateBoard(@RequestBody BoardDto boardDto) {
        return boardService.updateBoard(boardDto);
    }

    //게시글 삭제
    @RequestMapping(value="/board/delete/{id}", method=RequestMethod.DELETE)
    public void deleteBoard(@PathVariable("id") Long id) {
        boardService.deleteBoard(id);
    }

    //게시글 조회수 증가
    @RequestMapping(value="/board/increaseViewCount/{id}", method=RequestMethod.PUT)
    public BoardDto increaseViewCount(@PathVariable("id") Long id) {
        return boardService.getIncreaseViewCount(id);
    }
}
