## 请求规范

### 1. 请求方式

所有商户到我方平台的请求都是 POST + JSON  
所有我方到商户的回调通知也是 POST + JSON

### 2. 接口安全

商户开通后, 会拿到两个安全相关参数:  
appKey:  应用ID  
secret:  md5密钥

请求方(商户发起请求， 或者平台通知商户)请求步骤，

1. 先将请求的POST body 与 secret拼接得到待签名串toSign,
2. 然后对签名串toSign计算一个md5的hex表示的字符串作为签名sign
3. 发起POST请求时, 设置请求头 x-app-key为 appKey
4. 设置请求头x-sign为计算出来的签名sign
5. 发起post请求...

接收方需要校验签名, 校验步骤为:

1. 从收到的请求里拿到x-app-key, x-sign两个请求头, 以及请求的body
2. 将body + secret合并然后计算一个md5的hex字符串calc
3. 比较calc和x-sign是否相等

### 3. 应答响应结构

所有应答都是形如{code:0, data:{}, msg:"错误信息"}的格式
其中， code == 0, 代表后台处理正常    
code != 0 代表请求错误， 或者后端处理异常, 同时此时msg会有异常的错误描述

### 4. 收款处理的状态

| 状态  | 说明  |
|-----|-----|
| 0   | 处理中 |
| 1   | 处理中 |
| 2   | 成功  |
| 3   | 失败  |

### 5. 代付处理的状态

| 状态  | 说明  |
|-----|-----|
| 0   | 处理中 |
| 1   | 处理中 |
| 2   | 成功  |
| 3   | 失败  |

### 6. 各接口请求应答的字段说明， 
#### 6.1 收款
请求:  
```java
// 收款充值请求
public class ChargeRequest {
    private BigDecimal amount;     // 金额:  99.99
    private String orderId;        // 商户订单号
    private String notifyUrl;      // 回调地址
    private String callbackUrl;    // 跳转地址
    private String payCode;        // 支付代码:  upi
    private String memo;           // 其他信息:  可选填
}
```

应答:  
```java
// 充值应答
public class ChargeResponse {
    private Long id;            // 平台单号 - json表示为字符串
    private String payUrl;      // 支付链接
    private String upi;         // upi  可能有
    private String raw;         // 原始支付材料
}
```
### 6.2 收款查询
请求:
```java
public class ChargeQueryRequest {
    private Long id;         // 平订单号 - json表示为字符串
    private String orderId;  // 商户订单号
}
// 平台单号， 商户单号， 两种必填一个
```

应答:
```java
public class ChargeQueryResponse {
    private Long id;             // 平台单号
    private String orderId;        // 商户单号
    private Integer processStatus;  // 订单处理状态: 0, 1: 处理中， 2: 成功,  3: 超时
    private String utr;             // utr
    private String upi;             // upi
    private BigDecimal realAmount;  // 实际付款金额
}
```

### 6.4: 收款通知
请求:
```java
public class ChargeNotify {
    private Integer processStatus; //   订单状态: 0, 1: 处理中，  2: 成功,  3: 超时
    private Long id;   // 平台订单号
    private String orderId; // 商户订单号
    private String utr; // utr
    private String upi;  // upi
    private String realAmount;  // 实际付款金额
}
```
应答:
```
OK
```


### 6.5 代付
请求:
```java
public class WithdrawRequest {
    private String orderId;      // 商户单号
    private BigDecimal amount;   // 金额:  99.99
    private String accountUser;  // 账户名
    private String accountNo;    // 账户号
    private String accountBank;  // 银行:  可以填NA
    private String accountIfsc;  // IFSC
    private String notifyUrl;    // 回调url
    private String callbackUrl;  // 跳转url可以和notifyUrl一样
    private String memo;         // 可填NA
}
```
应答:
```java
public class WithdrawResponse {
    Long id;   // 平台单号
}
```

### 6.6 代付查询
请求:
```java
public class WithdrawQueryRequest {
    private Long id;             // 平台单号
    private String orderId;      // 商户单号
}
```
应答:
```java
public class WithdrawQueryResponse {
    private Long id;                  // 平台单号
    private String orderId;           // 商户单号
    private Integer processStatus;    // 订单处理状态
    private String utr;               // utr
    private String pictures;           // 付款凭证
    private BigDecimal amount;        // 付款金额
}
```

### 6.7 代付通知
请求:
```java
public class WithdrawNotify {
    private Integer processStatus;  // 订单处理状态
    private Long id;                // 平台单号
    private String orderId;         // 商户单号
    private String utr;             // utr
    private String pictures;         // 支付凭证地址: 可能有
    private BigDecimal amount;      // 代付金额
}
```
应答:
```
OK
```

### 6.8 余额查询

请求:
```javascript
{}
```
应答:
```java
public class BalanceResponse {
    private BigDecimal balance; // 余额数字
}
```


### 7. 接口地址:

| 接口   | 地址                      |
|------|-------------------------|
| 收款   | /sys/zapi/charge        |
| 收款查询 | /sys/zapi/chargeQuery   |
| 代付:  | /sys/zapi/withdraw      |
| 代付查询 | /sys/zapi/withdrawQuery |
| 余额查询 | /sys/zapi/balance       |

### 8. 注意事项:

8.1. 所有请求都是 POST+JSON, 余额查询 由于不需要请求参数， 请传递空的json对象{}  
8.2. 联调阶段,应答里有详细的错误信息， 包括我方签名串的内容，以及我方的签名值, 便于研发比对签名串和签名值  
8.3. 联调阶段,收款交易会自动回调, 付款交易会一半成功回调， 一半失败回调
8.4. Long类型在json序列化里是String表现形式!  
8.5. 校验签名必须用原始的body, 不能先解析然后又重新序列化body的字符串!  
