"""
대원 케이터링 프로젝트 자율 개선 에이전트
- inspector : 코드/빌드 로그 점검
- planner   : 수정 계획 수립
- developer : 계획대로 코드 수정
- tester    : Gradle 빌드 + 결과 보고

실행: python agents/auto-improve.py
요구사항: pip install claude-agent-sdk
"""

import asyncio
from claude_agent_sdk import query, ClaudeAgentOptions, AgentDefinition

PROJECT_ROOT = "c:/project/ngy"
MAX_RETRY = 3  # 빌드 실패 시 최대 재시도 횟수

AGENTS = {
    "inspector": AgentDefinition(
        description=(
            "Spring Boot 프로젝트의 코드, 템플릿, 빌드 로그를 분석해 "
            "버그·보안 취약점·개선 포인트를 찾는다. "
            "점검 요청이 들어오면 반드시 이 에이전트를 사용해라."
        ),
        prompt=(
            "당신은 Spring Boot 3 + Thymeleaf + JPA 전문 코드 점검가입니다.\n"
            "프로젝트 루트: " + PROJECT_ROOT + "\n\n"
            "점검 범위:\n"
            "  - src/main/java 의 Java 소스 (보안, NPE, 트랜잭션, SQL 인젝션)\n"
            "  - src/main/resources/templates 의 Thymeleaf 템플릿 (XSS, th: 오류)\n"
            "  - build.gradle 의 의존성 충돌·deprecated 라이브러리\n\n"
            "출력 형식:\n"
            "  [심각도: HIGH/MED/LOW] 파일경로:라인 — 문제 설명\n"
            "  → 권고 수정 방향\n\n"
            "문제가 없으면 '이상 없음' 이라고 출력하세요."
        ),
        tools=["Read", "Glob", "Grep", "Bash"],
    ),
    "planner": AgentDefinition(
        description=(
            "inspector의 점검 결과를 받아 구체적인 수정 계획을 수립한다. "
            "계획 수립 요청이 들어오면 반드시 이 에이전트를 사용해라."
        ),
        prompt=(
            "당신은 소프트웨어 아키텍트입니다. "
            "inspector가 보고한 문제 목록을 받아 수정 계획을 만드세요.\n\n"
            "계획 형식:\n"
            "  ## 수정 계획\n"
            "  1. [HIGH] 파일경로 — 구체적 수정 내용\n"
            "  2. [MED]  파일경로 — 구체적 수정 내용\n"
            "  ...\n\n"
            "규칙:\n"
            "  - 심각도 HIGH → MED → LOW 순 정렬\n"
            "  - 각 항목은 developer가 바로 실행할 수 있을 만큼 구체적으로\n"
            "  - 파일을 읽어 현재 코드를 확인한 뒤 계획 작성\n"
            "  - 아키텍처를 변경하는 대규모 리팩터링은 제안하지 말 것"
        ),
        tools=["Read", "Glob", "Grep"],  # 코드 확인만, 수정 불가
    ),
    "developer": AgentDefinition(
        description=(
            "planner의 수정 계획을 받아 실제 코드를 수정한다. "
            "코드 수정 요청이 들어오면 반드시 이 에이전트를 사용해라."
        ),
        prompt=(
            "당신은 Spring Boot + Thymeleaf 개발자입니다. "
            "수정 계획을 받아 파일을 직접 수정하세요.\n\n"
            "규칙:\n"
            "  - 계획에 명시된 파일만 수정 (범위 이탈 금지)\n"
            "  - 기존 코드 스타일과 들여쓰기를 유지\n"
            "  - Thymeleaf 문법: th:text, th:href, th:if 등 올바르게 사용\n"
            "  - 수정 후 각 파일의 변경 요약을 출력\n"
            "  - 확신이 없는 변경은 TODO 주석으로 표시하고 건너뜀"
        ),
        tools=["Read", "Edit", "Write", "Glob", "Grep"],
    ),
    "tester": AgentDefinition(
        description=(
            "코드 수정 후 Gradle 빌드를 실행해 성공/실패 여부를 보고한다. "
            "테스트 요청이 들어오면 반드시 이 에이전트를 사용해라."
        ),
        prompt=(
            "당신은 QA 엔지니어입니다. "
            "프로젝트 루트: " + PROJECT_ROOT + "\n\n"
            "작업:\n"
            "  1. cd " + PROJECT_ROOT + " 후 ./gradlew build -x test 실행\n"
            "  2. 빌드 성공 시: '빌드 성공' + 경고 목록 출력\n"
            "  3. 빌드 실패 시: '빌드 실패' + 오류 메시지 전문 출력\n\n"
            "출력 마지막 줄은 반드시 'RESULT: SUCCESS' 또는 'RESULT: FAILURE' 로 끝낼 것."
        ),
        tools=["Bash", "Read"],
    ),
}

ORCHESTRATOR_PROMPT = """
다음 순서로 이 Spring Boot 프로젝트를 자율 점검·개선해:

1. inspector 에이전트로 프로젝트 전체를 점검해.
2. 점검 결과를 planner 에이전트에게 전달해 수정 계획을 받아.
   - 점검 결과가 '이상 없음'이면 3~4단계를 건너뛰고 완료 메시지만 출력해.
3. 수정 계획을 developer 에이전트에게 전달해 코드를 수정해.
4. tester 에이전트로 빌드를 실행해.
   - RESULT: SUCCESS → 완료. 변경 요약을 출력해.
   - RESULT: FAILURE → 실패 원인을 inspector에게 다시 전달하고 1번부터 재시도해.
     (최대 {max_retry}회. 초과 시 '자동 수정 실패, 수동 확인 필요' 메시지 출력)
""".format(max_retry=MAX_RETRY)


async def main():
    print("=" * 60)
    print("대원 프로젝트 자율 개선 에이전트 시작")
    print("=" * 60)

    async for message in query(
        prompt=ORCHESTRATOR_PROMPT,
        options=ClaudeAgentOptions(
            allowed_tools=["Task"],  # 부모는 Task만 — 서브에이전트에 모든 실행 위임
            agents=AGENTS,
        ),
    ):
        # 중간 진행 상황 출력
        if hasattr(message, "content"):
            for block in message.content:
                if hasattr(block, "text"):
                    print(block.text, end="", flush=True)
        # 최종 결과 출력
        if hasattr(message, "result"):
            print("\n" + "=" * 60)
            print("최종 결과:")
            print(message.result)


if __name__ == "__main__":
    asyncio.run(main())
