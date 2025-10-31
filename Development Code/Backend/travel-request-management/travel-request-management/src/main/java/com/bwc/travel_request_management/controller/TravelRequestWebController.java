//package com.bwc.travel_request_management.controller;
//
//import com.bwc.travel_request_management.dto.TravelRequestDTO;
//import com.bwc.travel_request_management.entity.TravelRequest;
//import com.bwc.travel_request_management.service.TravelRequestService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.validation.BindingResult;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.UUID;
//
//@Controller
//@RequestMapping("/travel-request")
//@RequiredArgsConstructor
//public class TravelRequestWebController {
//
//    private final TravelRequestService service;
//
//    @GetMapping("/form")
//    public String showCreateForm(Model model) {
//        TravelRequestDTO travelRequest = new TravelRequestDTO();
//        model.addAttribute("travelRequest", travelRequest);
//        return "travel-request-form";
//    }
//
//    @PostMapping("/save")
//    public String saveRequest(@ModelAttribute("travelRequest") @Valid TravelRequestDTO dto, 
//                            BindingResult result, Model model) {
//        if (result.hasErrors()) {
//            model.addAttribute("travelRequest", dto);
//            return "travel-request-form";
//        }
//        
//        try {
//            service.createRequest(dto);
//            return "redirect:/travel-request/list";
//        } catch (Exception e) {
//            model.addAttribute("error", e.getMessage());
//            model.addAttribute("travelRequest", dto);
//            return "travel-request-form";
//        }
//    }
//
//    @GetMapping("/list")
//    public String listRequests(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size,
//            Model model) {
//        
//        Pageable pageable = PageRequest.of(page, size);
//        Page<TravelRequestDTO> requestsPage = service.getAllRequests(pageable);
//        
//        model.addAttribute("requests", requestsPage.getContent());
//        model.addAttribute("currentPage", page);
//        model.addAttribute("totalPages", requestsPage.getTotalPages());
//        model.addAttribute("totalItems", requestsPage.getTotalElements());
//        
//        return "travel-request-list";
//    }
//
//    @GetMapping("/view/{id}")
//    public String viewRequest(@PathVariable UUID id, Model model) {
//        try {
//            TravelRequestDTO request = service.getRequest(id);
//            model.addAttribute("request", request);
//            return "travel-request-details";
//        } catch (Exception e) {
//            model.addAttribute("error", "Travel request not found");
//            return "redirect:/travel-request/list";
//        }
//    }
//
//    @GetMapping("/edit/{id}")
//    public String showEditForm(@PathVariable UUID id, Model model) {
//        try {
//            TravelRequestDTO request = service.getRequest(id);
//            model.addAttribute("travelRequest", request);
//            return "travel-request-form";
//        } catch (Exception e) {
//            model.addAttribute("error", "Travel request not found");
//            return "redirect:/travel-request/list";
//        }
//    }
//
//    @PostMapping("/update/{id}")
//    public String updateRequest(@PathVariable UUID id, 
//                              @ModelAttribute("travelRequest") @Valid TravelRequestDTO dto,
//                              BindingResult result, Model model) {
//        if (result.hasErrors()) {
//            model.addAttribute("travelRequest", dto);
//            return "travel-request-form";
//        }
//        
//        try {
//            service.updateRequest(id, dto);
//            return "redirect:/travel-request/list";
//        } catch (Exception e) {
//            model.addAttribute("error", e.getMessage());
//            model.addAttribute("travelRequest", dto);
//            return "travel-request-form";
//        }
//    }
//
//    @GetMapping("/delete/{id}")
//    public String deleteRequest(@PathVariable UUID id, Model model) {
//        try {
//            service.deleteRequest(id);
//            model.addAttribute("success", "Travel request deleted successfully");
//        } catch (Exception e) {
//            model.addAttribute("error", "Error deleting travel request: " + e.getMessage());
//        }
//        return "redirect:/travel-request/list";
//    }
//
//    @GetMapping("/approve/{id}")
//    public String approveRequest(@PathVariable UUID id, Model model) {
//        try {
//            service.updateStatus(id, TravelRequest.RequestStatus.APPROVED);
//            model.addAttribute("success", "Travel request approved");
//        } catch (Exception e) {
//            model.addAttribute("error", "Error approving request: " + e.getMessage());
//        }
//        return "redirect:/travel-request/list";
//    }
//
//    @GetMapping("/reject/{id}")
//    public String rejectRequest(@PathVariable UUID id, Model model) {
//        try {
//            service.updateStatus(id, TravelRequest.RequestStatus.REJECTED);
//            model.addAttribute("success", "Travel request rejected");
//        } catch (Exception e) {
//            model.addAttribute("error", "Error rejecting request: " + e.getMessage());
//        }
//        return "redirect:/travel-request/list";
//    }
//}