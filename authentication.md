## Vue.js 로그인 구현

> vue.js로 만드는 SPA에 사용자 인증기능을 넣으면서 공부했던 내용으로, 백엔드에 대한 내용은 자세히 다루지 않았다. 



사용자 인증 기능을 위해 `vuex`를 이용하였기때문에 우선 `vuex`에 대해 알아보자. 



### Vuex란?

vue.js의 **모든 데이터 통신을 한 곳에서 중앙 집중식으로 관리**하는 상태관리를 위한 패턴 라이브러리이다. 

컴포넌트 기반 프레임워크에서는 컴포넌트 간 통신이나 데이터 전달을 조금 더 유기적으로 관리할 필요성이 있는데(vue는 기본적으로 상위-하위 컴포넌트 통신방식을 따른다), 이러한 관계를 **한 곳에서 관리하기 쉽게 구조화**하는 것이 **상태관리**이다. 

##### 구성요소

- `state`: 컴포넌트 간 공유될 data를 관리하는 속성
- `view`: 데이터가 표현될 template 속성
- `actions`: state 값을 변경하는 methods 속성 

##### 참조 

전역으로 등록된 `$store` 속성으로 vuex에 접근할 수 있다. 



### 로직

참고로 `토큰`은 만료시간을 정할 수 있기때문에 보안상 안전하다.  



##### 회원가입

1. FE => BE 회원정보 보내며 POST 요청
2. BE : 회원정보 검증 및 암호화 후 유저 테이블에 데이터 기입

##### 로그인

1. FE => BE 회원정보 보내며 POST 요청
2. BE : 회원정보 유저테이블에서 확인 후, 토큰 테이블에 토큰과 UID 기입
3. BE => FE 생성한 토큰 응답
4. FE : 토큰을 `localstorage` 혹은 `sessionstorage`에 저장 (로그인 유지)
5. 새로고침 시 FE => BE 요청에서 저장된 토큰을 보내서 유저 정보 받기 
6. 받은 유저정보 vuex에 저장 

```javascript
// store.js
...
actions: {
   // 로그인 -> 토큰 반환 
    login( {dispatch}, loginObj) {
        axios.post("", loginObj)
        .then(res => {
            // 성공 시 token 받기
            let token = res.data.token
            // 토큰 스토리지에 저장
            localStorage.setItem("access_token", token)
            dispatch("getMemberInfo")
        })
    },
    getMemberInfo({commit}) {
        // 스토리지에 저장되어있는 토큰 불러오기
        let token = localStorage.getItem("access_token")
        let config = {
            headers: {
                "access-token": token
            }
        }
        // 토큰 -> 멤버 정보 반환
        axios.get("", config)
        .then( res => {
            // 멤버 정보 저장
            let userInfo = {
                id : res.data.data.id,
                ...
            }
        })
        
    }
}
```

새로고침될 때마다 `getMemberInfo`를 실행시켜야하는데 실행시키기위한 코드의 위치는 여러가지 경우가 가능하나 일반적으로 `main.js`에서 실행한다.

```javascript
// main.js

...
new Vue({
    router,
    store,
    // 새로고침 시 
    beforCreate() {
        this.$store.dispatch('getMemberInfo')
    },
    render: h => h(App)
}).$mount("#app")
```



##### 로그아웃

1. FE => BE 토큰 보내며 POST 요청
2. BE : 해당 토큰을 테이블에서 삭제 후 200상태 코드 보내기
3. FE : 토큰 storage에서 삭제



### storage

- local storage : 브라우저 종료 시 유지 (자동 로그인 선택 시 이용)
- session storage : 브라우저 종료 시 초기화

##### 명령문

```jsx
localStorage.setItem('loginUID',데이터) // 생성

localStorage.getItem('loginUID') // 조회

localStorage.removeItem('loginUID') // 삭제
```