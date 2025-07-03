package vs_fundos.challenge.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vs_fundos.challenge.dto.OrderDTO;
import org.springframework.http.HttpStatus;
import vs_fundos.challenge.service.OrderService;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping("/{id}")
    @Operation(summary = "Get order by id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get order  successfully"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public  ResponseEntity<OrderDTO> getOrderById(
            @Parameter(description = "ID of the order to be retrieved")
            @PathVariable Long id
    ) {
        OrderDTO orderDTO = orderService.getOrderById(id);
        return new ResponseEntity<>(orderDTO, HttpStatus.OK);
    }

    @PostMapping("/")
    @Operation(summary = "Create a new order.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<OrderDTO> createOrder(@RequestBody OrderDTO orderDTO) {
        OrderDTO createdOrder  = orderService.createOrder(orderDTO);
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }

    @PostMapping("/random")
    @Operation(summary = "Create a new random order.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order created successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<OrderDTO> createRandomOrder() {
        OrderDTO createdOrder  = orderService.createRandomOrder();
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a order by id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<OrderDTO> updateOrder(
            @Parameter(description = "ID of the order to be updated")
            @PathVariable Long id,
            @RequestBody OrderDTO orderDetails
    ) {
        OrderDTO orderDTO = orderService.updateById(id, orderDetails);
        return new ResponseEntity<>(orderDTO, HttpStatus.OK);
    }
}
