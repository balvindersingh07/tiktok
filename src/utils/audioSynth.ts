// Tashan Web Audio Synthesizer
// Provides authentic looping music themes for TikTok sounds without external CDN assets

class SoundSynthesizer {
  private ctx: AudioContext | null = null;
  private isPlaying: boolean = false;
  private activeOscillators: OscillatorNode[] = [];
  private activeGainNodes: GainNode[] = [];
  private intervalId: number | null = null;
  private currentTrack: string = '';

  private getAudioContext(): AudioContext {
    if (!this.ctx) {
      const AudioContextClass = window.AudioContext || (window as unknown as { webkitAudioContext: typeof AudioContext }).webkitAudioContext;
      this.ctx = new AudioContextClass();
    }
    if (this.ctx.state === 'suspended') {
      this.ctx.resume();
    }
    return this.ctx;
  }

  public playSound(title: string, isMuted: boolean = false) {
    if (isMuted) {
      this.stop();
      return;
    }

    if (this.isPlaying && this.currentTrack === title) {
      return;
    }

    this.stop();
    this.currentTrack = title;
    this.isPlaying = true;

    try {
      const ctx = this.getAudioContext();

      // Different acoustic patterns based on sound title
      const isLofi = title.toLowerCase().includes('lofi') || title.toLowerCase().includes('kitchen');
      const isCinematic = title.toLowerCase().includes('cinematic') || title.toLowerCase().includes('mountain');
      const isSkate = title.toLowerCase().includes('skate') || title.toLowerCase().includes('sunset');

      const tempo = isLofi ? 550 : isCinematic ? 700 : isSkate ? 420 : 360; // ms per beat
      const baseFreq = isLofi ? 220 : isCinematic ? 164.81 : isSkate ? 196 : 130.81;

      // Note sequences (pentatonic / soulful)
      const scale = [1, 1.25, 1.333, 1.5, 1.667, 2, 2.25, 2.5];
      let step = 0;

      const playNote = () => {
        if (!this.isPlaying) return;

        const noteIndex = step % scale.length;
        const multiplier = scale[noteIndex];
        const freq = baseFreq * multiplier;

        const osc = ctx.createOscillator();
        const gain = ctx.createGain();

        osc.type = isLofi ? 'triangle' : isCinematic ? 'sine' : 'sawtooth';
        osc.frequency.setValueAtTime(freq, ctx.currentTime);

        const volume = isLofi ? 0.08 : isCinematic ? 0.07 : 0.05;
        gain.gain.setValueAtTime(volume, ctx.currentTime);
        gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + (tempo / 1000) * 0.9);

        osc.connect(gain);
        gain.connect(ctx.destination);

        osc.start();
        osc.stop(ctx.currentTime + (tempo / 1000));

        this.activeOscillators.push(osc);
        this.activeGainNodes.push(gain);

        // Keep buffer small
        if (this.activeOscillators.length > 20) {
          this.activeOscillators.splice(0, 10);
          this.activeGainNodes.splice(0, 10);
        }

        step++;
      };

      playNote();
      this.intervalId = window.setInterval(playNote, tempo);
    } catch {
      // Audio context might require user gesture first
    }
  }

  public stop() {
    this.isPlaying = false;
    this.currentTrack = '';
    if (this.intervalId !== null) {
      clearInterval(this.intervalId);
      this.intervalId = null;
    }
    this.activeOscillators.forEach((osc) => {
      try {
        osc.stop();
        osc.disconnect();
      } catch {}
    });
    this.activeGainNodes.forEach((gain) => {
      try {
        gain.disconnect();
      } catch {}
    });
    this.activeOscillators = [];
    this.activeGainNodes = [];
  }

  public getIsPlaying(): boolean {
    return this.isPlaying;
  }
}

export const soundSynth = new SoundSynthesizer();
