/*

접근 제어자	    같은 클래스	같은 패키지	    자식 클래스	다른 패키지 일반 클래스
---------------------------------------------------------------------------
private	            O	        X	            X	            X
default	            O	        O	            X	            X
protected	        O	        O	            O	            X
public	            O	        O	            O	            O
---------------------------------------------------------------------------



 * 학습용 프로젝트 이기에, 예외 원인을 바로 확인할 수 있도록 구현했다.
 * 보통 사용자를 위한 일정한 메시지를 전달한다. 아래와 같은 이유 때문이다.
        - 내부 클래스나 구현 정보 노출
        - DB 제약조건이나 SQL 정보 노출
        - 예외마다 응답 메시지가 들쭉날쭉함
        - 일반 사용자가 이해하기 어려움

 * 그래서 아래의 필드를 두고, 더 추가를 안하는 것 같다.
 private CommonResponse(boolean success, String message, T data) {
      this.success = success;
      this.message = message; // 보통 개발자가 정의한, 약속한 문자열이 삽입된다.
      this.data = data;
  }

 * 우리 팀은 code와 (error -> errorKind)를 추가하는 목적이 원인을 바로 확인하기 위함인 듯 하다.
 private final boolean success;
 private final String message;
 private final T data;
 private final int code;
 private final String errorKind;

 * 그래서 그냥 Exception 발생할 때에 메시지를 그대로 message 필드에 넣기로 결정했다.



 * log 레벨 : DEBUG < INFO < WARN < ERROR
 log.debug("디버깅 정보");
 log.info("정상적인 주요 동작");
 log.warn("주의가 필요한 상황");
 log.error("예상하지 못한 심각한 오류");

 log.debug("요청받은 userId: {}", userId);
 log.info("회원가입 완료: userId={}", userId);
 log.warn("BusinessException: {}", e.getMessage()); // 사용자 조회 실패나 닉네임 중복 같은 비즈니스 오류에 대하여 사용한다.
 log.error("Unexpected exception", e);  // 예외 객체 e를 넘기기 때문에, Exception Message와 Stack Trace 출력
 Stack Trace(예외 발생 과정에서 호출된 메서드들의 순서와 위치 정보를 나타내는 것)


 * 이 코드는 단일 Exception 처리를 한다. 하나의 Exception 안에 여러 오류가 들어있는 경우
 * CommonResponse에 List<ErrorDetail> errors; 필드를 추가하고,      // public record ErrorDetail(String field, String message) { }
 * fail 에 대한 단일 처리 함수(errors => null)와 다중 처리 함수(errors => list)를 분리하여 만든다.

 * 예를 들어 값을 받아오는 데까지 error를 모으고, 한 번에 검사하게 되는 로직을 작성하게 되면
 * errorKind는 개발자가 모은 에러를 아우르는 errorKind를 정의하고, (FAILED_SIGNUP_FIELDS) -> 이 errorKind에 대한 errors를 수집하게 된다.
 {
    "success": false,
    "code": 409,
    "message": "이미 사용 중인 회원 정보가 있습니다.",
    "data": null,
    "errorKind": "FAILED_SIGNUP_FIELDS",
    "errors": [
        {
            "field": "password",
            "message": "특수문자를 포함해야 합니다."
        },
        {
            "field": "email",
            "message": "이메일 형식이 올바르지 않습니다."
        }
    ]
 }
 * 보통 다중 오류는 많이 발생하지 않기 때문이다.
 * 정리하자면 Handler는 모두 단일 처리다. (Exception이 발생하면 동작을 못하기 때문)
 * 다만 이제 비즈니스 로직에서의 오류를 exception 처리할 때, 개발자가 end_point를 정해두고,
 * 그 전에는 list에다가 오류 정보를 담는 ErrorDetail 객체를 add한다.
 * 그리고 end_point에서 검사를 해서 오류라고 판단했을 때, list도 같이 전달하면 된다.
 if (!isValidPassword(request.password())) {
    errors.add(
        new ErrorDetail(
            "password",
            "특수문자를 포함해야 합니다."
        )
    );
 }
 if (!isValidEmail(request.email())) {
    errors.add(
        new ErrorDetail(
            "email",
            "이메일 형식이 올바르지 않습니다."
        )
    );
 }
 if (!errors.isEmpty()) {
    throw new MultipleBusinessException(
        HttpStatus.BAD_REQUEST,
        "INVALID_INPUT_VALUE",
        "입력값 검증에 실패했습니다.",
        errors
    );
 }
 * 근데 그러면 CommonResponse를 다시 설계해야하니 그러지 말자.
 * 굳이 하고 싶다면
 * 그냥 저기서 list로 모으고, 그냥 그 설명을 String으로 변환 후에 message로 받자.
 */