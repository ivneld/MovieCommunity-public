package Movie.MovieCommunity.JPADomain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity(name = "like_comment")
@Table(name = "like_comment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LikeComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_comment_id")
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    @Builder
    public LikeComment(Member member, Comment comment) {
        this.member = member;
        this.comment = comment;
    }
}
