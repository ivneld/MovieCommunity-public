import React, { useCallback, useState } from 'react';
import { Link } from 'react-router-dom';
import { LinkContainer, MovieSpan, CategorySpan } from './styles';
import useSWR from 'swr';
import fetcher from '../../utils/fetcher';
import MovieDetailModal from '../../components/MovieDetailModal';
import { useSearchParams } from 'react-router-dom';

const Main = () => {
    const [showMovieDetailModal,setShowMovieDetailModal] = useState(false);
    const [content, setContent] = useState('');

    const [postingBoardMovieId, setPostingBoardMovieId] = useState('');

    const [isEvery, setIsEvery] = useState(true);
    const [isForeign, setIsForeign] = useState(false);
    const [isKorea, setIsKorea] = useState(false);

	const [searchParams, setSearchParams] = useSearchParams(); // 쿼리 스트링을 searchParams 형태로 가져오고
	const movienm = searchParams.get('movienm'); // movienm 값 변수에 저장

    const [posts, setPosts] = useState([]);

    const onCloseModal = useCallback(() => {
        setShowMovieDetailModal(false);
    }, []);

    const onClickModal = useCallback((e) => {
        setShowMovieDetailModal(true);
        setContent('아직 상세데이터 못 받음');
    }, []);

    const { data : mainData } = useSWR('http://localhost:8080', fetcher, {
        dedupingInterval: 100000,
    });

    if (movienm){ // 게시판에서 영화 제목 검색 O 경우
        const result = mainData.filter(data => data.movieNm === `${movienm}`) // 검색한 영화 제목 filter해서 해당 영화 정보만 불러오기
        return (
            <div>
                {result.length === 1 && result.map((data)=>{ // 검색한 영화가 존재 O 경우
                        return(
                            <>
                                <div>{data.movieNm}</div>
                                <div>{data.ranking}</div>
                                <div>{data.rankInten}</div>
                                <div>{data.openDt}</div>
                                <div>{data.audiAcc}</div>
                            </>
                        )}
                    )
                }
                {result.length !== 1 && // 검색한 영화가 존재 X 경우
                    <div>검색하신 영화가 존재하지 않습니다.</div>
                }
            </div>
        )
    }

    
    if (!mainData){
        return <div>데이터가 없습니다</div>
    }   

    return(
        <>
            {!movienm && // 메인페이지 (게시판에서 영화 제목 검색 X 경우)
            <>
                <LinkContainer>
                    <span>장르별&nbsp;&nbsp;</span>
                    <Link to='/boards'>게시판&nbsp;&nbsp;</Link>
                    <span>My Page&nbsp;&nbsp;</span>
                    <Link to='/auth/login'>로그인</Link>
                </LinkContainer>
                <hr/>
                <div>
                    Top 10 movie list
                </div>
                <hr/>
                    <CategorySpan onClick={() => (setIsEvery(true), setIsForeign(false), setIsKorea(false))}>전체영화&nbsp;&nbsp;&nbsp;</CategorySpan>
                    <CategorySpan onClick={() => (setIsEvery(false), setIsForeign(false), setIsKorea(true))}>국내영화&nbsp;&nbsp;&nbsp;</CategorySpan>
                    <CategorySpan onClick={() => (setIsEvery(false), setIsForeign(true), setIsKorea(false))}>해외영화</CategorySpan>
                <hr/>
         
                {isEvery &&
                    <div>
                        {mainData[0].map((data)=>{
                                return(
                                    <div>
                                        <MovieSpan onClick={() => (onClickModal(), setPostingBoardMovieId(data.movieNm))}>{data.movieNm}</MovieSpan>
                                    </div>
                                )}
                            )
                        }
                    </div>                     
                }
                {isForeign &&
                    <div>
                        {mainData[1].map((data)=>{
                                return(
                                    <div>
                                        <MovieSpan onClick={() => (onClickModal(), setPostingBoardMovieId(data.movieNm))}>{data.movieNm}</MovieSpan>
                                    </div>
                                )}
                            )
                        }
                    </div>                     
                }
                {isKorea &&
                    <div>
                        {mainData[2].map((data)=>{
                                return(
                                    <div>
                                        <MovieSpan onClick={() => (onClickModal(), setPostingBoardMovieId(data.movieNm))}>{data.movieNm}</MovieSpan>
                                    </div>
                                )}
                            )
                        }
                    </div>                     
                }
                <hr/>
                <h3>
                    {posts.map((post, index) => (
                    <div key={index}>{post.content}</div>
                    ))}
                </h3>
                <MovieDetailModal
                    show={showMovieDetailModal}
                    onCloseModal={onCloseModal}
                    setShowMovieDetailModal={setShowMovieDetailModal}
                    content={content}
                    postingBoardMovieId={postingBoardMovieId}
                />
            </>
            }
        </>
    )
};

export default Main;