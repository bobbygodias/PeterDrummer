using PeterDrummer.Data;
using PeterDrummer.InputSystem;
using PeterDrummer.Rhythm;
using UnityEngine;

namespace PeterDrummer.Core
{
    /// <summary>
    /// Aplica presets de dificuldade em janela de hit, velocidade e multiplicador de score.
    /// </summary>
    public class DifficultyController : MonoBehaviour
    {
        [SerializeField] private PlayerInputController inputController;
        [SerializeField] private NoteSpawner noteSpawner;
        [SerializeField] private ScoreSystem scoreSystem;
        [SerializeField] private GameDifficulty currentDifficulty = GameDifficulty.Normal;

        private void Awake()
        {
            ApplyDifficulty(currentDifficulty);
        }

        public void SetEasy() => ApplyDifficulty(GameDifficulty.Easy);
        public void SetNormal() => ApplyDifficulty(GameDifficulty.Normal);
        public void SetHard() => ApplyDifficulty(GameDifficulty.Hard);

        public void ApplyDifficulty(GameDifficulty difficulty)
        {
            currentDifficulty = difficulty;

            switch (difficulty)
            {
                case GameDifficulty.Easy:
                    inputController.ConfigureHitWindows(0.065, 0.120);
                    noteSpawner.SetScrollSpeed(6.0f);
                    scoreSystem.ConfigureScoreMultiplier(0.90f);
                    break;

                case GameDifficulty.Normal:
                    inputController.ConfigureHitWindows(0.045, 0.090);
                    noteSpawner.SetScrollSpeed(8.0f);
                    scoreSystem.ConfigureScoreMultiplier(1.00f);
                    break;

                case GameDifficulty.Hard:
                    inputController.ConfigureHitWindows(0.030, 0.065);
                    noteSpawner.SetScrollSpeed(10.5f);
                    scoreSystem.ConfigureScoreMultiplier(1.20f);
                    break;
            }
        }
    }
}
