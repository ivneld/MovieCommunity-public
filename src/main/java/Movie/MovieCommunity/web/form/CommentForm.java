package Movie.MovieCommunity.web.form;

import Movie.MovieCommunity.JPADomain.Board;
import Movie.MovieCommunity.JPADomain.Comment;
import Movie.MovieCommunity.JPADomain.JpaMovie;
import Movie.MovieCommunity.JPADomain.Member;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Data
@NoArgsConstructor
public class CommentForm {
    private Long id;
    @NotEmpty(message = "필수 값입니다.")
    private String content;

    private Member member;

    private JpaMovie movie;
    private Comment parent;
    @Builder
    public CommentForm( String content, Member member, JpaMovie movie, Comment parent) {

        this.content = content;
        this.member = member;
        this.movie = movie;
        this.parent = parent;
    }
}
