# runelite-flexxed
Extends `Flexo` by the RuneLite+ team. Feel free to make pull requests with any contributions or improvements you can think of.
The idea of this is to extend Flexo to add more functionality, and reduce the need to copy and re-use the same stock functions over-and-over in separate projects.

# Example
```java
    @Inject
    private Client client;
    @Inject
    private TabUtils tabUtils;
    @Inject
    private ItemManager itemManager;

    private ThreadPoolExecutor executorService = new ThreadPoolExecutor(1, 1, 1,
            TimeUnit.SECONDS, 
            new ArrayBlockingQueue(1), 
            new ThreadPoolExecutor.DiscardPolicy());
    private Bot bot;
    
    @Override
    protected void startUp(){
        executorService.submit(() -> {
            bot = null;

            try {
                bot = new Bot(client, tabUtils, itemManager);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
    @Override
    protected void shutDown(){
       bot = null;
    }
    
    @Subscribe
    public void onGameStateChanged(GameStateChanged event){
        GameState state = event.getGameState();

        if (state == GameState.CONNECTION_LOST || state == GameState.LOGIN_SCREEN || state == GameState.HOPPING) {
            return;
        }
        
        log.info("Inventory contents: {}", bot.getInventoryItems());
    }
```
