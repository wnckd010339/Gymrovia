package com.acorn.gymmanagement.member.controller;

import com.acorn.gymmanagement.common.pagination.PageRequest;
import com.acorn.gymmanagement.common.pagination.PageResult;
import com.acorn.gymmanagement.member.dto.request.MemberSearchRequest;
import com.acorn.gymmanagement.member.dto.response.MemberDetailResponse;
import com.acorn.gymmanagement.member.dto.response.MemberListResponse;
import com.acorn.gymmanagement.member.service.MemberService;
import com.acorn.gymmanagement.member.view.MemberDetailView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/members")
public class MemberController {

    private final MemberService memberService;

    @GetMapping
    public String list(
            @ModelAttribute MemberSearchRequest condition,
            @ModelAttribute PageRequest pageRequest,
            Model model
            ) {
        PageResult<MemberListResponse> result =
                memberService.search(condition, pageRequest);

        model.addAttribute("result", result);
        model.addAttribute("condition", condition);
        model.addAttribute("statusSummary", memberService.findStatusSummary());
        return "admin/member/list";
    }

     @GetMapping("/{memberId}")
    public String detail(
            @PathVariable Long memberId,
            @RequestParam(defaultValue = "basic") String tab,
            Model model
     ){
         String activeTab = "membership".equals(tab) ? "membership" : "basic";
         MemberDetailView detail =
                 memberService.findDetailView(memberId);

         model.addAttribute("detail", detail);
         model.addAttribute("activeTab", activeTab);

         return "admin/member/detail";
    }
}
