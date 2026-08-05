package org.scoula.saving.mapper;

import org.scoula.saving.domain.SavingAccountVO;

public interface SavingAccountMapper {
    // 군적금 계좌 등록. insert 후 vo.accountId에 생성된 id가 채워진다
    void insertAccount(SavingAccountVO vo);
}
