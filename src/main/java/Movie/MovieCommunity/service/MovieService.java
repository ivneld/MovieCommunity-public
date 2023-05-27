package Movie.MovieCommunity.service;

import Movie.MovieCommunity.JPADomain.*;
import Movie.MovieCommunity.JPARepository.MemberRepository;
import Movie.MovieCommunity.JPADomain.Comment;
import Movie.MovieCommunity.JPADomain.JpaWeeklyBoxOffice;
import Movie.MovieCommunity.JPARepository.MovieRepository;
import Movie.MovieCommunity.JPARepository.WeeklyBoxOfficeRepository;
import Movie.MovieCommunity.dataCollection.MovieDataService;
import Movie.MovieCommunity.util.CalendarUtil;
import Movie.MovieCommunity.web.apiDto.movie.entityDto.CreditDto;
import Movie.MovieCommunity.web.apiDto.movie.entityDto.SeriesDto;
import Movie.MovieCommunity.web.apiDto.movie.response.MovieDetailResponse;
import Movie.MovieCommunity.web.apiDto.movie.response.WeeklyRankingResponse;
import Movie.MovieCommunity.web.apiDto.movie.response.YearRankingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class MovieService {
    private final MovieRepository movieRepository;
    private final MemberRepository memberRepository;
    private final MovieDataService movieDataService;
    private final WeeklyBoxOfficeRepository weeklyBoxOfficeRepository;

    public List<YearRankingResponse> yearRanking(int openDt, Long memberId){
        Member member;
        if(memberId != null){
            member = memberRepository.findById(memberId).orElseThrow();
        } else {
            member = null;
        }
        int startDt = openDt*10000;
        int endDt = (openDt+1)*10000;
        List<Movie> yearRankingByOpenDt = movieRepository.findYearRankingByOpenDt(startDt, endDt);
        List<YearRankingResponse> response = new ArrayList<>();
        String url = null;
        int i = 1;
        for (Movie m : yearRankingByOpenDt) {
            long count = m.getLikeMovies().stream().filter(lm -> lm.getMember() == member).count();

            int topCnt = 0;
            YearRankingResponse yearRankingResponse = YearRankingResponse.builder()
                    .rank(i)
                    .id(m.getId())
                    .movieNm(m.getMovieNm())
                    .showTm(m.getShowTm())
                    .openDt(m.getOpenDt())
                    .prdtStatNm(m.getPrdtStatNm())
                    .voteAverage(m.getVoteAverage())
                    .watchGradeNm(m.getWatchGradeNm())
                    .overview(m.getOverview().length() > 80 ? m.getOverview().substring(0, 80)+"..." : m.getOverview())
                    .posterPath(m.getPosterPath())
                    .interest(m.getLikeMovies().size())
                    .myInterest(count > 0 ? true : false)
                    .build();
            if (i==1 && m.getVideos() != null) {
                url = m.getVideos().get(0).getUrl();
                yearRankingResponse.setUrl(url);
            }
            for (Comment comment : m.getComments()) {
                if(topCnt < comment.getLikeComments().size()){
                    topCnt = comment.getLikeComments().size();
                    yearRankingResponse.setTopComment(comment.getContent());
                }
            }
            i++;
            response.add(yearRankingResponse);
        }
        return response;
    }
    @Transactional
    public MovieDetailResponse movieDetail(Long movieId, Long memberId){
        try {Member member;
            if(memberId != null){
                member = memberRepository.findById(memberId).orElseThrow();
            } else {
                member = null;
            }

            Movie movie = movieRepository.findById(movieId).orElseThrow(() -> new EntityNotFoundException("영화가 없습니다."));
            long count = movie.getLikeMovies().stream().filter(lm -> lm.getMember() == member).count();
            MovieDetailResponse movieDetailResponse = MovieDetailResponse.builder()
                    .id(movie.getId())
                    .tmId(movie.getTmId())
                    .posterPath(movie.getPosterPath())
                    .interest(movie.getLikeMovies().size())
                    .myInterest(count > 0 ? true : false)
                    .movieNm(movie.getMovieNm())
                    .openDt(movie.getOpenDt())
                    .voteAverage(movie.getVoteAverage())
                    .watchGradeNm(movie.getWatchGradeNm())
                    .showTm(movie.getShowTm())
                    .nationNm(movie.getNationNm())
                    .audiAcc(movie.getAudiAcc())
                    .overview(movie.getOverview())
                    .build();
            if (movie.getVideos().size() != 0){movieDetailResponse.setVideoUrl(movie.getVideos().get(0).getUrl());}
            for (JpaMovieWithCompany mc : movie.getMovieWithCompanies()) {
                if(mc.getCompany().getCompanyPartNm().equals("제작사")){
                    movieDetailResponse.addCompany(mc.getCompany().getCompanyNm());
                }
            }
            for (JpaMovieWithGenre mg : movie.getMovieWithGenres()) {
                movieDetailResponse.addGenre(mg.getGenre().getGenreNm());
            }
            for (MovieWithCredit mc : movie.getMovieWithCredits()) {
                Credit credit = mc.getCredit();
                movieDetailResponse.addCredit(new CreditDto(credit.getId(), credit.getActorNm(), credit.getProfile_path(), credit.getCreditCategory()));
            }
            movieDetailResponse.setWeeklyRanks(getWeeklyRankingResponses(movie));
            if(movie.getCollectionId()!=null && movie.getCollectionId() != 0) {
                List<SeriesDto> seriesDtos = movieDataService.selectSeries(movie.getCollectionId());
                for (SeriesDto seriesDto : seriesDtos) {
                    movieDataService.collectByTmId(seriesDto.getId());
                }
                movieDetailResponse.setSeries(seriesDtos);
            }
            return movieDetailResponse;
        }catch(EntityNotFoundException e){

        }

        return null;
    }

    /**
     * @param year -> 주간별로 분할할 년도
     * @param week -> 원하는 주차
     * @return -> YearRankingResponse List
     * (weeklyBoxOffice, movie 에서 존재하는 영화는 다를 수 있다.)
     */
    public List<YearRankingResponse> weeklyRanking(int year, int week) {
        LocalDate startDate = LocalDate.of(year, 1, 1);
        TemporalField weekOfYear = WeekFields.of(Locale.KOREA).weekOfYear();

        int startWeek = startDate.get(weekOfYear);
        int targetWeek = startWeek + week - 1;

        LocalDate firstDayOfWeek = startDate.with(weekOfYear, targetWeek);
        LocalDate lastDayOfWeek = firstDayOfWeek.plusDays(6);

        List<JpaWeeklyBoxOffice> weeklyBoxOffices = weeklyBoxOfficeRepository.findByOpenDtBetween(firstDayOfWeek, lastDayOfWeek);

        List<YearRankingResponse> result = new ArrayList<>();

        for (JpaWeeklyBoxOffice weeklyBoxOffice : weeklyBoxOffices) {
            if (movieRepository.findByMovieCd(weeklyBoxOffice.getMovieCd()).isPresent()) {
                Movie movie = movieRepository.findByMovieCd(weeklyBoxOffice.getMovieCd()).get();

                result.add(YearRankingResponse.builder()
                        .id(movie.getId())
                        .movieNm(movie.getMovieNm())
                        .showTm(movie.getShowTm())
                        .openDt(movie.getOpenDt())
                        .prdtStatNm(movie.getPrdtStatNm())
                        .watchGradeNm(movie.getWatchGradeNm())
                        .overview(movie.getOverview())
                        .posterPath(movie.getPosterPath())
                        .voteAverage(movie.getVoteAverage())
                        .build());
            }
        }
        return result;
    }

    public List<WeeklyRankingResponse> findWeeklyRank(Long movieId) {
        Movie movie = movieRepository.findById(movieId).orElseThrow();
        return getWeeklyRankingResponses(movie);
    }

    private List<WeeklyRankingResponse> getWeeklyRankingResponses(Movie movie) {
        List<JpaWeeklyBoxOffice> weeklyList = weeklyBoxOfficeRepository.findByMovieCdOrderByYearWeekTime(movie.getMovieCd());
        List<WeeklyRankingResponse> weeklyRankingResponses = new ArrayList<>();
        for (JpaWeeklyBoxOffice w : weeklyList) {
            System.out.println("w = " + w);
            WeeklyRankingResponse response = WeeklyRankingResponse.builder()
                    .rank(w.getRanking())
                    .rankInten(w.getRankInten())
                    .rankOldAndNew(w.getRankOldAndNew())
                    .build();
            String date = w.getShowRange().substring(0, 8);
            System.out.println("date = " + date);
            int year = Integer.parseInt(date.substring(0, 4));
            int month = Integer.parseInt(date.substring(4, 6));
            int day = Integer.parseInt(date.substring(6, 8));
            String currentWeekOfMonth = CalendarUtil.getCurrentWeekOfMonth(year,month,day);
            String[] split = currentWeekOfMonth.split(",");
            String week = year +"년 "+ split[0]+"월 "+split[1]+"주차";

            response.setWeek(week);
            weeklyRankingResponses.add(response);
        }
        return weeklyRankingResponses;
    }
}