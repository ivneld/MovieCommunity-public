package Movie.MovieCommunity.community.service;


import Movie.MovieCommunity.JPADomain.Member;
import Movie.MovieCommunity.community.domain.Comment;
import Movie.MovieCommunity.community.domain.SubComment;
import Movie.MovieCommunity.community.domain.SubCommentLike;
import Movie.MovieCommunity.community.dto.SubCommentRequestDto;
import Movie.MovieCommunity.community.repository.SubCommentLikeRepository;
import Movie.MovieCommunity.community.repository.SubCommentRepository;
import Movie.MovieCommunity.community.response.ResponseDto;
import Movie.MovieCommunity.community.response.SubCommentResponseDto;
import Movie.MovieCommunity.service.CommentService;
import Movie.MovieCommunity.service.auth.CustomTokenProviderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubCommentService {


    private final SubCommentRepository subCommentRepository;

    private final SubCommentLikeRepository subCommentLikeRepository;

    private final CustomTokenProviderService tokenProvider;

    private final CommunityCommentService commentService;

    //대댓글 등록하기
    @Transactional
    public ResponseDto<?> createReComment(SubCommentRequestDto requestDto, HttpServletRequest request) {

        if (null == request.getHeader("Authorization")) {
            return ResponseDto.fail("MEMBER_NOT_FOUND",
                    "로그인이 필요합니다.");
        }

        Member member = validateMember(request);
        if (null == member) {
            return ResponseDto.fail("INVALID_TOKEN", "Token이 유효하지 않습니다.");
        }

        Comment comment = commentService.isPresentComment(requestDto.getCommentId());

        if (null == comment) {
            return ResponseDto.fail("NOT_FOUND", "존재하지 않는 코멘트 id 입니다.");
        }

        SubComment subComment = SubComment.builder()
                .member(member)
                .comment(comment)
                .subComment(requestDto.getSubComment())
                .build();

        System.out.println(subComment.getComment());

        subCommentRepository.save(subComment);

        return ResponseDto.success(
                SubCommentResponseDto.builder()
                        .id(subComment.getId())
                        .author(subComment.getMember().getNickname())
                        .subComment(subComment.getSubComment())
                        .createdAt(subComment.getCreatedDate())
                        .modifiedAt(subComment.getModifiedDate())
                        .build());
    }

    // 대댓글 조회하기
    public ResponseDto<?> getAllSubcommentsByComment(Long commentId) {
        Comment comment = commentService.isPresentComment(commentId);
        if (null == comment) {
            return ResponseDto.fail("NOT_FOUND", "존재하지 않는 댓글 id 입니다.");
        }

        List<SubComment> subCommentList = subCommentRepository.findAllByComment(comment);
        List<SubCommentResponseDto> subCommentResponseDtoList = new ArrayList<>();

        //대댓글 조회까진 되는데 좋아요 추가 미완성
        List<SubCommentLike> subCommentForLike = new ArrayList<>();

        for (SubComment subCommentEach : subCommentList) {
            int cntLikes_subcomment = subCommentLikeRepository.countLikeBySubCommentId(subCommentEach.getId());
            subCommentResponseDtoList.add(
                    SubCommentResponseDto.builder()
                            .id(subCommentEach.getId())
                            .author(subCommentEach.getMember().getNickname())
                            .subComment(subCommentEach.getSubComment())
                            .cntLikes(cntLikes_subcomment)
                            .createdAt(subCommentEach.getCreatedDate())
                            .modifiedAt(subCommentEach.getModifiedDate())
                            .build()
            );

        }
        return ResponseDto.success(subCommentResponseDtoList);
    }

    //대댓글 수정하기
    @Transactional
    public ResponseDto<?> updateSubComment(Long id, SubCommentRequestDto subCommentRequestDto, HttpServletRequest request) {
        if (null == request.getHeader("Refresh-Token")) {
            return ResponseDto.fail("MEMBER_NOT_FOUND",
                    "로그인이 필요합니다.");
        }

        if (null == request.getHeader("Authorization")) {
            return ResponseDto.fail("MEMBER_NOT_FOUND",
                    "로그인이 필요합니다.");
        }

        Member member = validateMember(request);
        if (null == member) {
            return ResponseDto.fail("INVALID_TOKEN", "Token이 유효하지 않습니다.");
        }

        SubComment subComment = isPresentSubComment(id);
        if (null == subComment) {
            return ResponseDto.fail("NOT_FOUND", "존재하지 않는 대댓글 id 입니다.");
        }

        if (subComment.validateMember(member)) {
            return ResponseDto.fail("BAD_REQUEST", "작성자만 수정할 수 있습니다.");
        }

        subComment.update(subCommentRequestDto);
        return ResponseDto.success(
                SubCommentResponseDto.builder()
                        .id(subComment.getId())
                        .author(subComment.getMember().getNickname())
                        .subComment(subComment.getSubComment())
                        .createdAt(subComment.getCreatedDate())
                        .modifiedAt(subComment.getModifiedDate())
                        .build()
        );
    }

    //대댓글 삭제하기
    @Transactional
    public ResponseDto<?> deleteSubComment(Long id, HttpServletRequest request) {
        if (null == request.getHeader("Refresh-Token")) {
            return ResponseDto.fail("MEMBER_NOT_FOUND",
                    "로그인이 필요합니다.");
        }
        if (null == request.getHeader("Authorization")) {
            return ResponseDto.fail("MEMBER_NOT_FOUND",
                    "로그인이 필요합니다.");
        }
        Member member = validateMember(request);
        if (null == member) {
            return ResponseDto.fail("INVALID_TOKEN", "Token이 유효하지 않습니다.");
        }
        SubComment subComment = isPresentSubComment(id);
        if (null == subComment) {
            return ResponseDto.fail("NOT_FOUND", "존재하지 않는 대댓글 id 입니다.");
        }

        if (subComment.validateMember(member)) {
            return ResponseDto.fail("BAD_REQUEST", "작성자만 삭제할 수 있습니다.");
        }

        subCommentRepository.delete(subComment);
        return ResponseDto.success("success");
    }
    @Transactional(readOnly = true)
    public SubComment isPresentSubComment(Long id) {
        Optional<SubComment> optionalSubComment = subCommentRepository.findById(id);
        return optionalSubComment.orElse(null);
    }

    @Transactional
    public Member validateMember(HttpServletRequest request){
        if (!tokenProvider.validateToken(request.getHeader("Refresh-Token"))) {
            return null;
        }
        return tokenProvider.getMemberFromAuthentication();
    }
}
