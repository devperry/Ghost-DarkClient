package me.client.module.util.utilities;
public class TimerUtil {
    private long lastTime;
    private long delayTime;
    /**
     * Constructor de TimerUtil. Inicializa el tiempo a 0.
     */
    public TimerUtil() {
        this.lastTime = 0;
        this.delayTime = 0;
    }
    /**
     * Establece un nuevo delay (o duración del cooldown) en milisegundos.
     *
     * @param delayInMillis El tiempo de delay/cooldown en milisegundos.
     */
    public void setDelay(long delayInMillis) {
        this.delayTime = delayInMillis;
    }
    /**
     * Reinicia el timer, estableciendo el 'lastTime' al tiempo actual.
     * Esto es útil para reiniciar un cooldown o delay.
     */
    public void reset() {
        this.lastTime = System.currentTimeMillis();
    }
    /**
     * Verifica si el cooldown configurado (delay) ha expirado.
     *
     * @return true si el cooldown ha expirado, false si no.
     */
    public boolean hasCooldownExpired() {
        return System.currentTimeMillis() - this.lastTime >= this.delayTime;
    }
    /**
     * Verifica si el cooldown configurado aún está activo.
     *
     * @return true si el cooldown está activo (NO ha expirado), false si el cooldown ha terminado.
     */
    public boolean isCoolingDown() {
        return !hasCooldownExpired();
    }
    /**
     * Verifica si el cooldown ha expirado y reinicia el timer si es así.
     * Esto es útil para ejecutar una acción periódicamente con un cooldown y reiniciarlo automáticamente.
     *
     * @return true si el cooldown ha expirado y el timer se ha reiniciado, false si el cooldown no ha expirado.
     */
    public boolean hasCooldownExpiredAndReset() {
        if (hasCooldownExpired()) {
            reset();
            return true;
        }
        return false;
    }
}