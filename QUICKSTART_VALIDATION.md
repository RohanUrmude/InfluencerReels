# Quick Start: Model Validation System

## 🚀 Get Started in 5 Minutes

### Step 1: Enable in Your Service
```java
@Service
public class ContentGenerationService {
    @Autowired
    private ModelValidationService validationService;
    
    public String generateContent() {
        // Start tracking
        validationService.startModelTracking("mistralai/Mistral-7B-Instruct-v0.2");
        
        // Get response
        String response = mistralService.generateContent(...);
        
        // Record it
        validationService.recordModelResponse("mistralai/Mistral-7B-Instruct-v0.2", response, 1250);
        
        // Save metrics
        validationService.savePerformanceMetrics("mistralai/Mistral-7B-Instruct-v0.2");
        
        return response;
    }
}
```

### Step 2: Check Performance
```bash
# Get all models performance
curl http://localhost:8081/api/models/validation/performance/all | jq
```

Response shows:
- Average latency per model
- Token usage trends
- Reliability scores
- Health status

### Step 3: Compare Models
```bash
curl -X POST http://localhost:8081/api/models/validation/compare \
  -H "Content-Type: application/json" \
  -d '{
    "mistralai/Mistral-7B-Instruct-v0.2": "Response from Mistral...",
    "meta-llama/Meta-Llama-3-8B-Instruct": "Response from Llama...",
    "microsoft/Phi-3-mini-4k-instruct": "Response from Phi..."
  }' \
  -G --data-urlencode "prompt=Generate viral TikTok script"
```

Claude will evaluate and rank them!

---

## 📚 Common Scenarios

### Scenario 1: Track Phi Model
```java
validationService.startModelTracking("microsoft/Phi-3-mini-4k-instruct");
String analysis = phiService.analyzeAudience(...);
validationService.recordModelResponse("microsoft/Phi-3-mini-4k-instruct", analysis, 850);
validationService.savePerformanceMetrics("microsoft/Phi-3-mini-4k-instruct");

// Later: Get report
Map report = validationService.getModelPerformanceReport("microsoft/Phi-3-mini-4k-instruct");
System.out.println("Phi Reliability: " + report.get("reliabilityScore"));
System.out.println("Phi Avg Latency: " + report.get("averageLatencyMs") + "ms");
```

### Scenario 2: Auto-Track with Interceptor
```java
@Autowired
private ModelResponseInterceptor interceptor;

// Just call this - validation happens in background!
public void processModelResponse(String modelName, String prompt, String response) {
    interceptor.interceptResponse(modelName, prompt, response, tokensUsed);
    // Your code continues immediately
}
```

### Scenario 3: Score Individual Response
```bash
curl -X POST http://localhost:8081/api/models/validation/score \
  -H "Content-Type: application/json" \
  -d "Here is the generated script content..." \
  -G --data-urlencode "modelName=mistralai/Mistral-7B-Instruct-v0.2" \
  -G --data-urlencode "context=educational script"

# Response: { "score": 8.5 }
```

### Scenario 4: Get Claude's Evaluation
```bash
curl -X POST http://localhost:8081/api/models/validation/validate \
  -H "Content-Type: application/json" \
  -d "Generated script content..." \
  -G --data-urlencode "modelName=meta-llama/Meta-Llama-3-8B-Instruct" \
  -G --data-urlencode "prompt=Generate educational content for fitness niche"

# Response: { 
#   "evaluation": "Claude's detailed analysis of quality, relevance, accuracy..."
# }
```

---

## 📊 What You Get

### Per Model:
```
✅ Total Requests: How many times used
✅ Success Rate: Reliability percentage  
✅ Avg Latency: Response time in ms
✅ Avg Tokens: Token consumption
✅ Health Status: Is model performing well?
✅ Last Used: When was it last called?
```

### Performance Comparison:
```
Model          Requests  Success Rate  Latency  Tokens  Status
Mistral        156       97%          3,420ms  1,380   ✅ Healthy
Llama          143       96%          4,120ms  1,520   ✅ Healthy
Phi            98        97%          1,850ms  890     ✅ Healthy
```

---

## 🎯 Use Cases

**Case 1: Which model is fastest?**
```
Phi: 1,850ms ← FASTEST (33% faster than Llama)
Mistral: 3,420ms
Llama: 4,120ms
```

**Case 2: Which model uses least tokens?**
```
Phi: 890 tokens ← MOST EFFICIENT
Mistral: 1,380 tokens
Llama: 1,520 tokens
```

**Case 3: Which model is most reliable?**
```
Mistral: 97% ← MOST RELIABLE
Phi: 97%
Llama: 96%
```

**Case 4: Which model produces best quality?**
```
Run comparison → Claude evaluates → See detailed analysis
```

---

## 🔧 Configuration

### Enable/Disable Validation
```java
@Autowired
private ModelResponseInterceptor interceptor;

// Disable validation (useful for testing)
interceptor.setValidationEnabled(false);

// Re-enable later
interceptor.setValidationEnabled(true);

// Check status
if (interceptor.isValidationEnabled()) {
    System.out.println("Validation is active");
}
```

### Clear Metrics for New Session
```bash
curl -X POST http://localhost:8081/api/models/validation/clear
```

---

## 📈 Example: Daily Performance Report

```java
public void generateDailyReport() {
    Map<String, Object> allModels = validationService.getAllModelsPerformance();
    
    for (var entry : allModels.entrySet()) {
        String modelName = entry.getKey();
        Map<String, Object> stats = (Map) entry.getValue();
        
        System.out.println("Model: " + modelName);
        System.out.println("  Requests: " + stats.get("totalRequests"));
        System.out.println("  Success Rate: " + stats.get("reliabilityScore"));
        System.out.println("  Avg Latency: " + stats.get("averageLatencyMs") + "ms");
        System.out.println("  Health: " + (stats.get("isHealthy") ? "✅" : "⚠️"));
        System.out.println();
    }
}
```

---

## 🎓 Real World Example

**Goal**: Generate content with Mistral, validate with Claude, and track performance

```java
@Service
public class OptimizedContentService {
    @Autowired private MistralService mistralService;
    @Autowired private ModelValidationService validationService;
    
    public String generateViralContent(ContentRequest request) {
        // Start timer
        long startTime = System.currentTimeMillis();
        validationService.startModelTracking("mistralai/Mistral-7B-Instruct-v0.2");
        
        // Generate content
        String script = mistralService.generateContent(
            request.getTopic(),
            request.getPlatform(),
            request.getNiche()
        );
        
        // Record metrics
        int tokensUsed = estimateTokens(script);
        validationService.recordModelResponse(
            "mistralai/Mistral-7B-Instruct-v0.2",
            script,
            tokensUsed
        );
        
        // Get Claude's evaluation
        String evaluation = validationService.validateResponseWithClaude(
            "mistralai/Mistral-7B-Instruct-v0.2",
            "Generate viral " + request.getPlatform() + " content",
            script
        );
        
        // Score it
        BigDecimal score = validationService.scoreModelResponse(
            "mistralai/Mistral-7B-Instruct-v0.2",
            script,
            request.getNiche() + " content for " + request.getPlatform()
        );
        
        // Save performance data
        validationService.savePerformanceMetrics("mistralai/Mistral-7B-Instruct-v0.2");
        
        // Log results
        log.info("Content generated. Quality Score: {}/10, Evaluation: {}",
                 score, evaluation.substring(0, 100));
        
        return script;
    }
}
```

---

## 🔍 Monitoring Dashboard (Optional)

Once you have performance data, you can create a dashboard:

```sql
-- Get today's performance
SELECT model_name, 
       request_count,
       success_count,
       reliability_score,
       average_latency_ms
FROM model_performance_logs
WHERE DATE(updated_at) = CURRENT_DATE;

-- Get trend over 7 days
SELECT model_name, DATE(updated_at), AVG(average_latency_ms)
FROM model_performance_logs
WHERE updated_at > NOW() - INTERVAL 7 DAY
GROUP BY model_name, DATE(updated_at);
```

---

## ⚡ Tips & Best Practices

1. **Always record tokens used** - Helps track cost
2. **Save metrics after each request** - Keep data fresh
3. **Compare models regularly** - Find which works best
4. **Monitor reliability score** - Get alerts if <80%
5. **Use interceptor for non-critical paths** - Doesn't block
6. **Query reports before making model changes** - Data-driven decisions

---

## 🆘 Troubleshooting

**Q: Validation not happening?**
A: Check if ModelValidationService is auto-wired and validation is enabled

**Q: Claude API errors?**
A: Set `CLAUDE_API_KEY` environment variable or system will use mock evaluations

**Q: Performance data not saving?**
A: Check database connection and `model_performance_logs` table exists

**Q: Async validation too slow?**
A: Switch to manual integration for immediate results

---

## 📝 Next Steps

1. ✅ Inject ModelValidationService into your services
2. ✅ Track first model response
3. ✅ Check REST API endpoint
4. ✅ Compare models
5. ✅ Use data to optimize model selection
6. ✅ Create monitoring dashboard (optional)

**You're all set!** Start tracking today! 🚀
