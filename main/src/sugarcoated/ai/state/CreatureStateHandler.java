package sugarcoated.ai.state;

import sugarcoated.ai.CreatureAI;

public class CreatureStateHandler {
    protected final CreatureAI ai;

    public CreatureState currentState = CreatureState.WANDER;
    public CreatureState previousState = CreatureState.WANDER;

    public CreatureStateHandler(CreatureAI ai) {
        this.ai = ai;
    }

    public CreatureState get(){
        return currentState;
    }

    public void transition(CreatureState next){
        if(next == currentState) return;

        ai.exitState(currentState);

        previousState = currentState;
        currentState = next;

        ai.enterState(next);
    }

    public void update(){
        ai.updateState(currentState);
    }

    public boolean isState(CreatureState state){
        return currentState == state;
    }
}
