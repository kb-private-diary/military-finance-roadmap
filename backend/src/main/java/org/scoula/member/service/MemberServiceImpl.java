package org.scoula.member.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.scoula.common.exception.BusinessException;
import org.scoula.common.util.RankCalculator;
import org.scoula.dashboard.domain.VacationVO;
import org.scoula.dashboard.mapper.DashboardMapper;
import org.scoula.member.domain.MilitaryTypeVO;
import org.scoula.member.domain.TermsAgreementVO;
import org.scoula.member.domain.TermsVO;
import org.scoula.member.dto.ChangePasswordRequestDTO;
import org.scoula.member.dto.FindIdRequestDTO;
import org.scoula.member.dto.FindIdResponseDTO;
import org.scoula.member.dto.FindPasswordRequestDTO;
import org.scoula.member.dto.MemberDTO;
import org.scoula.member.dto.MemberJoinDetailRequestDTO;
import org.scoula.member.dto.MemberJoinRequestDTO;
import org.scoula.member.dto.MemberUpdateRequestDTO;
import org.scoula.member.dto.MilitaryTypeDTO;
import org.scoula.member.dto.MilitaryUnitDTO;
import org.scoula.member.dto.TermsDTO;
import org.scoula.member.dto.WithdrawRequestDTO;
import org.scoula.member.mapper.MemberMapper;
import org.scoula.member.mapper.MilitaryTypeMapper;
import org.scoula.member.mapper.MilitaryUnitMapper;
import org.scoula.member.mapper.RankMapper;
import org.scoula.member.mapper.TermsMapper;
import org.scoula.security.account.domain.MemberVO;
import org.scoula.security.account.dto.AuthResultDTO;
import org.scoula.security.account.dto.UserInfoDTO;
import org.scoula.security.account.mapper.UserDetailsMapper;
import org.scoula.security.util.JwtProcessor;

@Log4j2
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    // 아이디를 이메일 형식으로 받는다 (로컬파트 + @ + 도메인)
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");
    // 8자 이상 + 숫자 1개 이상 + 특수문자(영문·숫자 외 문자) 1개 이상
    private static final Pattern PASSWORD_HAS_DIGIT = Pattern.compile(".*\\d.*");
    private static final Pattern PASSWORD_HAS_SPECIAL = Pattern.compile(".*[^A-Za-z0-9].*");

    private final PasswordEncoder passwordEncoder;
    private final MemberMapper mapper;
    private final TermsMapper termsMapper;
    private final MilitaryTypeMapper militaryTypeMapper;
    private final MilitaryUnitMapper militaryUnitMapper;
    private final RankMapper rankMapper;
    private final DashboardMapper dashboardMapper;
    private final JwtProcessor jwtProcessor;
    private final UserDetailsMapper userDetailsMapper;

    private void validateEmailFormat(String userId) {
        if (userId == null || !EMAIL_PATTERN.matcher(userId).matches()) {
            throw BusinessException.badRequest("사용할 수 없는 아이디입니다.", "MEM_005");
        }
    }

    private void validatePasswordPolicy(String password) {
        boolean valid = password != null
                && password.length() >= 8
                && PASSWORD_HAS_DIGIT.matcher(password).matches()
                && PASSWORD_HAS_SPECIAL.matcher(password).matches();
        if (!valid) {
            throw BusinessException.badRequest(
                    "비밀번호는 8자 이상, 숫자와 특수문자를 포함해야 합니다.", "MEM_006");
        }
    }

    // 전화번호는 한 사람당 하나여야 하므로(아이디찾기 등에서 사람을 특정하는 기준이 됨) 계정 간 중복을 막는다.
    private void validatePhoneNotDuplicated(String phone) {
        if (this.mapper.countByPhone(phone) > 0) {
            throw BusinessException.conflict("이미 사용중인 전화번호입니다.", "MEM_008");
        }
    }

    @Override
    public boolean checkDuplicate(String userId) {
        return this.mapper.findByUserId(userId) != null;
    }

    @Override
    public MemberDTO findMember(String userId) {
        MemberVO member = Optional.ofNullable(this.mapper.get(userId))
                .orElseThrow(() -> BusinessException.notFound("일치하는 정보가 없습니다.", "MEM_001"));
        return MemberDTO.of(member);
    }

    // 1단계: 기본정보만 확인하고 계정 생성x, 실제 생성은 createMember(2단계)
    @Override
    public void checkJoinBasic(MemberJoinRequestDTO basic) {
        this.validateEmailFormat(basic.getUserId());
        if (this.checkDuplicate(basic.getUserId())) {
            throw BusinessException.conflict("이미 사용중인 아이디입니다.", "MEM_002");
        }
        this.validatePhoneNotDuplicated(basic.getPhone());
        this.validatePasswordPolicy(basic.getPassword());
        if (!basic.getPassword().equals(basic.getPasswordConfirm())) {
            throw BusinessException.badRequest("비밀번호가 일치하지 않습니다.", "MEM_004");
        }
    }

    @Transactional
    @Override
    public Long createMember(MemberJoinDetailRequestDTO dto) {
        // 1단계(checkJoinBasic)를 거치지 않고 바로 호출되는 경우까지 대비해 실제 계정 생성 시점에도 재검증한다.
        this.validateEmailFormat(dto.getUserId());
        if (this.checkDuplicate(dto.getUserId())) {
            throw BusinessException.conflict("이미 사용중인 아이디입니다.", "MEM_002");
        }
        this.validatePhoneNotDuplicated(dto.getPhone());
        this.validatePasswordPolicy(dto.getPassword());
        if (!dto.getPassword().equals(dto.getPasswordConfirm())) {
            throw BusinessException.badRequest("비밀번호가 일치하지 않습니다.", "MEM_004");
        }

        List<Long> requiredTermsIds = this.termsMapper.findRequiredIds();
        List<Long> agreedTermsIds = dto.getAgreedTermsIds() == null ? List.of() : dto.getAgreedTermsIds();
        if (!agreedTermsIds.containsAll(requiredTermsIds)) {
            throw BusinessException.badRequest("필수 약관에 모두 동의해야 합니다.", "MEM_003");
        }

        MemberVO member = dto.toVO();
        member.setPassword(this.passwordEncoder.encode(member.getPassword()));
        // 계급은 입력받지 않으므로(SignupMilitaryPage 참고) 입대일 기준으로 가입 시점에 바로 산정해둔다.
        // 그래야 익일 배치(RankPromotionScheduler) 전까지 계급이 비어 보이는 문제가 없다.
        if (member.getEnlistDate() != null) {
            int monthsSinceEnlist = RankCalculator.monthsSinceEnlist(member.getEnlistDate(), LocalDate.now());
            member.setRankId(this.rankMapper.findRankIdByServiceMonths(monthsSinceEnlist));
        }
        this.mapper.insert(member);
        this.createRegularVacation(member);

        if (!agreedTermsIds.isEmpty()) {
            Map<Long, String> versionByTermsId = this.termsMapper.findAll().stream()
                    .collect(Collectors.toMap(TermsVO::getTermsId, TermsVO::getVersion));
            for (Long termsId : agreedTermsIds) {
                TermsAgreementVO agreement = new TermsAgreementVO();
                agreement.setUserId(member.getId());
                agreement.setTermsId(termsId);
                agreement.setAgreed(true);
                agreement.setTermsVersion(versionByTermsId.get(termsId));
                agreement.setCreatedNm(member.getUserId());
                this.termsMapper.insertAgreement(agreement);
            }
        }

        return member.getId();
    }

    // 정기휴가(연가)는 입대하면 군종별 규정 일수만큼 자동으로 부여된다.
    // vacation 테이블에 REGULAR 마스터(부여) 행을 하나 만들어두면, 이후 사용내역은
    // DashboardService.createVacation()이 이 마스터 행의 잔여일수를 깎아가며 관리한다.
    private void createRegularVacation(MemberVO member) {
        if (member.getEnlistDate() == null) {
            return;
        }
        MilitaryTypeVO militaryType = this.militaryTypeMapper.findMilitaryType(member.getTypeId());
        if (militaryType == null) {
            return;
        }

        VacationVO vacation = VacationVO.builder()
                .userId(member.getId())
                .vacationCate("REGULAR")
                .vacationName("정기휴가")
                .vacationGet(member.getEnlistDate())
                .vacationDay(militaryType.getRegularVacationDays())
                .build();
        vacation.setCreatedNm(member.getUserId());

        this.dashboardMapper.insertVacation(vacation);
    }

    @Override
    public List<TermsDTO> findTerms() {
        return this.termsMapper.findAll().stream().map(TermsDTO::of).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<MilitaryTypeDTO> findMilitaryTypeList() {
        return this.militaryTypeMapper.findMilitaryTypeList().stream()
                .map(MilitaryTypeDTO::of).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<MilitaryUnitDTO> findMilitaryUnitListByTypeId(Integer typeId) {
        if (this.militaryTypeMapper.findMilitaryType(typeId) == null) {
            throw BusinessException.notFound("존재하지 않는 군종입니다.", "MEM_012");
        }
        return this.militaryUnitMapper.findMilitaryUnitListByTypeId(typeId).stream()
                .map(MilitaryUnitDTO::of).toList();
    }

    @Override
    public AuthResultDTO refresh(String refreshToken) {
        // 한 번만 파싱해서 얻은 Claims를 재활용 - 유효성 검증 + refresh여부 확인 + username추출을 각각 다시 파싱하지 않는다.
        Claims claims = this.jwtProcessor.parseClaims(refreshToken);
        if (!this.jwtProcessor.isRefreshToken(claims)) {
            throw new BadCredentialsException("refresh token이 아닙니다.");
        }

        String userId = this.jwtProcessor.getUsername(claims);
        MemberVO member = this.userDetailsMapper.get(userId);
        if (member == null) {
            throw new UsernameNotFoundException(userId + "은 없는 id입니다.");
        }

        String newAccessToken = this.jwtProcessor.generateToken(userId);
        String newRefreshToken = this.jwtProcessor.generateRefreshToken(userId);
        return new AuthResultDTO(newAccessToken, newRefreshToken, UserInfoDTO.of(member));
    }

    @Override
    public FindIdResponseDTO findUserId(FindIdRequestDTO request) {
        List<MemberVO> matches = this.mapper.findByNameAndPhone(request.getName(), request.getPhone());
        // 동명이인 등으로 여러 건이 매칭되면 특정 계정을 안전하게 골라낼 수 없으므로,
        // 개인정보 보호 차원에서 매칭 없음과 동일하게 처리한다.
        if (matches.size() != 1) {
            throw BusinessException.notFound("일치하는 회원 정보가 없습니다.", "MEM_007");
        }
        return new FindIdResponseDTO(maskUserId(matches.get(0).getUserId()));
    }

    @Transactional
    @Override
    public void resetPassword(FindPasswordRequestDTO request) {
        // 본인확인: 아이디+이름+전화번호가 전부 일치해야 한다.
        MemberVO member = this.mapper.get(request.getUserId());
        boolean identityMatches = member != null
                && member.getName().equals(request.getName())
                && member.getPhone().equals(request.getPhone());
        if (!identityMatches) {
            throw BusinessException.notFound("일치하는 회원 정보가 없습니다.", "MEM_007");
        }

        this.validatePasswordPolicy(request.getNewPassword());
        if (!request.getNewPassword().equals(request.getNewPasswordConfirm())) {
            throw BusinessException.badRequest("비밀번호가 일치하지 않습니다.", "MEM_004");
        }

        String encoded = this.passwordEncoder.encode(request.getNewPassword());
        this.mapper.updatePassword(member.getId(), encoded, member.getUserId());
    }

    @Transactional
    @Override
    public void updateMember(String userId, MemberUpdateRequestDTO request) {
        MemberVO member = Optional.ofNullable(this.mapper.get(userId))
                .orElseThrow(() -> BusinessException.notFound("일치하는 정보가 없습니다.", "MEM_001"));

        // 전화번호를 실제로 바꾸는 경우에만 중복 체크한다 (그대로면 자기 자신과 충돌로 오탐).
        if (!member.getPhone().equals(request.getPhone())) {
            this.validatePhoneNotDuplicated(request.getPhone());
        }

        member.setName(request.getName());
        member.setPhone(request.getPhone());
        member.setUnitName(request.getUnitName());
        member.setUnitCode(request.getUnitCode());
        member.setModifiedNm(userId);
        this.mapper.updateProfile(member);
    }

    @Transactional
    @Override
    public void changePassword(String userId, ChangePasswordRequestDTO request) {
        MemberVO member = Optional.ofNullable(this.mapper.get(userId))
                .orElseThrow(() -> BusinessException.notFound("일치하는 정보가 없습니다.", "MEM_001"));

        if (!this.passwordEncoder.matches(request.getCurrentPassword(), member.getPassword())) {
            throw BusinessException.badRequest("비밀번호가 일치하지 않습니다.", "MEM_010");
        }
        if (this.passwordEncoder.matches(request.getNewPassword(), member.getPassword())) {
            throw BusinessException.badRequest("새 비밀번호는 이전 비밀번호와 달라야 합니다.", "MEM_011");
        }
        this.validatePasswordPolicy(request.getNewPassword());
        if (!request.getNewPassword().equals(request.getNewPasswordConfirm())) {
            throw BusinessException.badRequest("비밀번호가 일치하지 않습니다.", "MEM_004");
        }

        String encoded = this.passwordEncoder.encode(request.getNewPassword());
        this.mapper.updatePassword(member.getId(), encoded, userId);
    }

    @Transactional
    @Override
    public void withdraw(String userId, WithdrawRequestDTO request) {
        MemberVO member = Optional.ofNullable(this.mapper.get(userId))
                .orElseThrow(() -> BusinessException.notFound("일치하는 정보가 없습니다.", "MEM_001"));

        if (!this.passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw BusinessException.badRequest("현재 비밀번호가 올바르지 않습니다", "MEM_010");
        }

        log.info("회원 탈퇴 처리 - userId: {}, reason: {}", userId, request.getReason());
        this.mapper.withdraw(member.getId(), userId);
    }

    // 개인정보 보호를 위해 아이디의 일부만 보여준다. 이메일 형식이면 @ 앞부분만, 아니면 절반만 마스킹.
    private String maskUserId(String userId) {
        int atIndex = userId.indexOf('@');
        if (atIndex <= 0) {
            int visible = Math.max(1, userId.length() / 2);
            return userId.substring(0, visible) + "*".repeat(userId.length() - visible);
        }
        String local = userId.substring(0, atIndex);
        String domain = userId.substring(atIndex);
        int visible = Math.min(2, local.length());
        return local.substring(0, visible) + "*".repeat(local.length() - visible) + domain;
    }
}
