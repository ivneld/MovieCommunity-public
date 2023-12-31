package Movie.MovieCommunity.community.domain;

import Movie.MovieCommunity.JPADomain.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class SubCommentLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "member_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @JoinColumn(name = "sub_comment_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private SubComment subComment;

    public boolean validateMember(Member member) {
        return !this.member.equals(member);
    }
}