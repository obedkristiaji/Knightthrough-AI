/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package knightthroughai;

import java.util.concurrent.ThreadLocalRandom;

import game.Game;
import main.collections.FVector;
import main.collections.FastArrayList;
import util.AI;
import util.Context;
import util.Move;
import util.Trial;
import util.state.State;
import util.state.containerState.ContainerState;
import utils.AIUtils;

/**
 *
 * @author :
 * Obed Kristiaji Sudarman - 6181801003
 * Muhamad Ariq Pratama - 6181801054
 */

/**
 * Kelas AlphaBetaSearch merupakan agent yang mengimplementasikan alpha beta.
 * Metode pencarian alpha beta dengan menggunakan iterative deepening yang
 * digunakan di kelas ini mengambil referensi dari:
 * https://github.com/Ludeme/LudiiAI/blob/master/AI/src/search/minimax/AlphaBetaSearch.java
 */
public class AlphaBetaSearch extends AI {

    // Variabel yang kita gunakan untuk menginisialisasi alpha (hampir seperti negatif tak hingga).
    private static final float ALPHA_INIT = -1000000.f;

    // Variabel yang kita gunakan untuk menginisialisasi beta (hampir seperti positif tak hingga).
    private static final float BETA_INIT = -ALPHA_INIT;

    // Variabel untuk menyimpan nilai heuristik yang akan kita hitung.
    private Heuristics heuristicValueFunction = null;

    // Variabel untuk menyimpan list dari move yang bisa dicapai di root saat ini, menggunakan FastArrayList dari main.collection.FastArrayList Ludii.
    protected FastArrayList<Move> currentRootMoves = null;

    // Variabel untuk menyimpan move terakhir yang dididapat.
    protected Move lastReturnedMove = null;

    // Variabel untuk menyimpan list dari move yang sudah terurut yang bisa dicapai di root saat ini, menggunakan FastArrayList dari main.collection.FastArrayList Ludii.
    protected FastArrayList<Move> sortedRootMoves = null;

    // Variabel untuk mengecek apakah kita sudah mencari di seluruh tree.
    protected boolean searchedFullTree = false;

    /**
     * Constructor
     */
    public AlphaBetaSearch() {
        // Memberi nama pada agent untuk ditunjukkan di Ludii.
        friendlyName = "Knightthrough AI";
    }

    /**
     * Method yang diambil dari util.AI Ludii, bertujuan untuk mendapatkan move
     * yang akan dijalankan.
     *
     * @param game Variabel Game dari game.Game Ludii berisi referensi game yang
     * dimainkan.
     * @param context Variabel Context dari util.Context Ludii berisi context
     * (termasuk state) dari game yang dimainkan.
     * @param maxSeconds Variabel berisi waktu maksimal yang dimiliki oleh tiap
     * player.
     * @param maxIteratons Variabel berisi iterasi maksimal yang bisa dilakukan
     * oleh tiap player.
     * @param maxDepth Variabel berisi kedalaman maksimal yang bisa dicapai oleh
     * tiap player.
     *
     * @return Move yang akan dijalankan setelah pencarian selesai dilakukan.
     */
    @Override
    public Move selectAction(
            final Game game,
            final Context context,
            final double maxSeconds,
            final int maxIterations,
            final int maxDepth
    ) {
        // Variabel yang digunakan untuk menyimpan limit kedalaman dari pencarian.
        // Jika maxDepth lebih besar dari 0, depthLimit = maxDepth, jika tidak lebih besar dari 0, depthLimit = tak hingga.
        final int depthLimit = maxDepth > 0 ? maxDepth : Integer.MAX_VALUE;

        // Mengecek waktu maksimal yang dimiliki, apakah melebihi 0 detik atau tidak.
        if (maxSeconds > 0) {
            // Jika waktu maksimal yang dimiliki masih lebih dari 0 detik akan dilakukan iterative deepening dengan startDepth = 1.
            lastReturnedMove = iterativeDeepening(game, context, maxSeconds, depthLimit, 1);

            // Mengembalikan move hasil perhitungan alpha beta dengan iterative deepening.
            return lastReturnedMove;
        } else {
            // Jika waktu maksimal yang dimiliki tidak lebih dari 0 detik akan dilakukan iterative deepening dengan startDepth = depthLimit.
            lastReturnedMove = iterativeDeepening(game, context, maxSeconds, depthLimit, depthLimit);

            // Mengembalikan move hasil perhitungan alpha beta dengan iterative deepening.
            return lastReturnedMove;
        }
    }

    /**
     * Method untuk melakukan pencarian alpha beta dengan penelusuran tree
     * menggunakan metode iterative deepening.
     *
     * @param game Variabel Game dari game.Game Ludii berisi referensi game yang
     * dimainkan.
     * @param context Variabel Context dari util.Context Ludii berisi context
     * (termasuk state) dari game yang dimainkan.
     * @param maxSeconds Variabel berisi waktu maksimal yang dimiliki oleh tiap
     * player.
     * @param maxDepth Variabel berisi kedalaman maksimal yang bisa dicapai oleh
     * tiap player.
     * @param startDepth Variabel berisi kedalaman awal yang digunakan oleh tiap
     * player ketika memulai perhitungan.
     *
     * @return Move hasil pencarian alpha beta.
     */
    public Move iterativeDeepening(
            final Game game,
            final Context context,
            final double maxSeconds,
            final int maxDepth,
            final int startDepth
    ) {
        // Variabel berisi waktu dimulainya pencarian.
        final long startTime = System.currentTimeMillis();
        // Variabel berisi waktu dimana pencarian harus berhenti.
        // Jika maxSeconds lebih besar dari 0, stopTime = startTime + maxSecond(dalam millisecond), jika tidak lebih besar dari 0, stopTime = tak hingga.
        long stopTime = (maxSeconds > 0.0) ? startTime + (long) (maxSeconds * 1000) : Long.MAX_VALUE;

        // Inisialisasi variabel currentRootMoves dengan move yang bisa dicapai di root saat ini.
        currentRootMoves = new FastArrayList<Move>(game.moves(context).moves());

        // Variabel sementara untuk menyimpan list dari move yang bisa dicapai di root saat ini,
        // tempMovesList = currentRootMoves, digunakan sebagai wadah untuk mengacak moves yang berasal dari currentRootMoves, menggunakan FastArrayList dari main.collection.FastArrayList Ludii.
        final FastArrayList<Move> tempMovesList = new FastArrayList<Move>(currentRootMoves);
        // Inisialisasi variabel sortedRootMoves sepanjang currentRootMoves.
        sortedRootMoves = new FastArrayList<Move>(currentRootMoves.size());
        // Melakukan looping sampai tempMovesList kosong.
        while (!tempMovesList.isEmpty()) {
            // Selama tempMovesList belum kosong sortedRootMoves diisi dengan move yang diambil secara random dari tempMovesList.
            sortedRootMoves.add(tempMovesList.removeSwap(ThreadLocalRandom.current().nextInt(tempMovesList.size())));
        }

        // Variabel untuk menyimpan panjang list move.
        final int numRootMoves = sortedRootMoves.size();

        // Variabel untuk menyimpan score dari tiap move menggunakan FVector dari main.collection.FVector Ludii.
        final FVector moveScores = new FVector(numRootMoves);
        // Variabel untuk menyimpan kedalaman pencarian saat ini.
        int searchDepth = startDepth - 1;
        // Variabel untuk menyimpan player yang harus dimaksimalkan, menggunakan method playerToAgent(index player) dari State di util.state.State Ludii.
        final int maximisingPlayer = context.state().playerToAgent(context.state().mover());

        // Variabel untuk menyimpan move terbaik yang ditemukan sampai saat ini.
        Move bestMoveCompleteSearch = sortedRootMoves.get(0);

        // Memulai iterasi penelusuran tree dari kedalaman saat ini sampai kedalaman maksimal.
        while (searchDepth < maxDepth) {
            // Menambah kedalaman pencarian saat ini di tiap iterasi pencarian.
            ++searchDepth;
            // Inisialisasi variabel searchedFullTree
            searchedFullTree = true;

            // Variabel untuk menyimpan score tertinggi selama pencarian.
            float score = ALPHA_INIT;
            // Variabel untuk menyimpan alpha selama pencarian.
            float alpha = ALPHA_INIT;
            // Variabel untuk menyimpan beta selama pencarian.
            final float beta = BETA_INIT;

            // Variabel untuk menyimpan move terbaik selama pencarian.
            Move bestMove = sortedRootMoves.get(0);

            // Memulai iterasi penelusuran move yang tersedia pada tiap tingkat kedalaman pencarian.
            for (int i = 0; i < numRootMoves; ++i) {
                // Variabel untuk menyimpan copy dari Context game yang sedang dimainkan.
                final Context copyContext = new Context(context);
                // Variabel untuk menyimpan move yang sedang dicapai saat pencarian.
                final Move m = sortedRootMoves.get(i);
                // Method untuk meng-apply move yang terpilih saat ini, menggunakan method apply(context, move) dari Game di game.Game Ludii.
                game.apply(copyContext, m);
                // Variabel untuk menyimpan hasil evaluasi tiap state yang di dapat dengan method alphaBeta.
                final float value = alphaBeta(copyContext, searchDepth - 1, alpha, beta, maximisingPlayer, stopTime);

                // Mengecek apakah waktu pencarian sampai sekarang sudah lebih dari atau sama dengan stopTime.
                if (System.currentTimeMillis() >= stopTime) {
                    // Set bestMove dengan null untuk menandakan iterasi terhenti.
                    bestMove = null;
                    // Hentikan iterasi
                    break;
                }

                // Mengisi moveScore dengan evaluasi terakhir yang didapat.
                moveScores.set(i, value);

                // Mengecek apakah hasil evaluasi yang didapat lebih dari score saat ini. 
                if (value > score) {
                    // Mengisi score saat ini dengan value.
                    score = value;
                    // Mengisi bestMove dengan move terakhir yang didapat(move terbaik dengan hasil evaluasi yang lebih dari score)
                    bestMove = m;
                }

                // Mengecek apakah alpha lebih rendah dari score saat ini, untuk menandakan batas bawah baru.
                if (score > alpha) {
                    // Mengisi alpha dengan score.
                    alpha = score;
                }

                // Mengecek apakah beta lebih rendah atau sama dengan alpha, karena jika beta sudah lebih rendah dari alpha tidak ada gunanya mencari lagi.
                if (alpha >= beta) {
                    // Hentikan iterasi
                    break;
                }
            }

            // Mengecek apakah iterasi tidak terhenti secara paksa yang ditandai dengan bestMove != null
            if (bestMove != null) {
                // Mengecek hasil pencarian alphabeta.
                if (score == BETA_INIT) {
                    // Jika score == tak hingga, sudah pasti kita memenangkan pertandingan,
                    // maka kita mengembalikan bestMove yang didapat ketika melakukan search.
                    return bestMove;
                } else if (score == ALPHA_INIT) {
                    // Jika score == min tak hingga, sudah pasti kita kalah dalam pertandingan,
                    // maka kita menunda kekalahan dengan mengembalikan bestMoveCompleteSearch yang didapat sejauh ini.
                    return bestMoveCompleteSearch;
                } else if (searchedFullTree) {
                    // Jika kita sudah mencari ke seluruh tree tetapi tidak mendapatkan hasil pasti antara menang atau kalah(kemungkinan draw),
                    // maka kita mengembalikan bestMove yang didapat ketika melakukan search.
                    return bestMove;
                }

                // Mengisi bestMoveCompleteSearch dengan bestMove saat ini agar terus terupdate.
                bestMoveCompleteSearch = bestMove;
            } else {
                // Jika terhenti secara paksa kita akan mengurangi kedalaman pencarian karena pencarian sebelumnya gagal dilakukan.
                --searchDepth;
            }

            // Mengecek apakah waktu pencarian sampai sekarang sudah lebih dari atau sama dengan stopTime.
            if (System.currentTimeMillis() >= stopTime) {
                // Mengembalikan bestMoveCompleteSearch yang didapat sejauh ini.
                return bestMoveCompleteSearch;
            }

            // Mengosongkan kembali moveScore
            moveScores.fill(0, numRootMoves, 0.f);
        }

        // Mengembalikan bestMoveCompleteSearch yang didapat sejauh ini.
        return bestMoveCompleteSearch;
    }

    /**
     * Method untuk melakukan pencarian nilai evaluasi alpha beta secara
     * rekursif.
     *
     * @param context Variabel Context dari util.Context Ludii berisi context
     * (termasuk state) dari game yang dimainkan.
     * @param depth Variabel berisi kedalaman yang akan digunakan dalam
     * pencarian.
     * @param inAlpha Variabel berisi nilai alpha yang akan digunakan dalam
     * pencarian.
     * @param inBeta Variabel berisi nilai beta yang akan digunakan dalam
     * pencarian.
     * @param maximisingPlayer Variabel untuk menyimpan player yang harus
     * dimaksimalkan.
     * @param stopTime Variabel berisi waktu dimana pencarian harus berhenti.
     *
     * @return hasil evaluasi dari pencarian yang dilakukan dengan fungsi
     * heuristic.
     */
    public float alphaBeta(
            final Context context,
            final int depth,
            final float inAlpha,
            final float inBeta,
            final int maximisingPlayer,
            final long stopTime
    ) {
        // Variabel Trial dari util.Trial Ludii yang digunakan untuk menjalankan trial.
        final Trial trial = context.trial();
        // Variabel State dari util.state.State Ludii yang digunakan untuk menyimpan state saat ini.
        final State state = context.state();

        // Mengecek apakah trial sudah selesai dilakukan atau apakah context untuk maximisingPlayer sudah tidak aktif.
        if (trial.over() || !context.active(maximisingPlayer)) {
            // Terminal state, maka dikembalikan utilities dari context saat terminal state, menggunakan method agentUtilities(context) dari AIUtils di utils.AIUtils Ludii.
            return (float) AIUtils.agentUtilities(context)[maximisingPlayer] * BETA_INIT;
        } else if (depth == 0) {
            // Jika depth = 0, searchedFullTree diisi dengan false.
            searchedFullTree = false;

            // Variabel untuk menyimpan hasil perhitungan evaluasi dengan fungi heuristic.
            float heuristicScore = heuristicValueFunction.computeValue(
                    context, maximisingPlayer);

            // Iterasi untuk mencari pemain lawan diantara pemain.
            for (int opp = 1; opp <= 2; opp++) {
                // Mengecek apakah pemain saat ini merupakan maximisingPlayer.
                if (opp != maximisingPlayer) {
                    // Jika bukan maka akan dicek apakah context dari opp(pemain lawan) sedang aktif.
                    if (context.active(opp)) {
                        // Melanjutkan perhitungan evaluasi dengan fungi heuristic pada pemain lawan.
                        heuristicScore -= heuristicValueFunction.computeValue(context, opp);
                    }
                }
            }

            // Mengecek apakah maximisingPlayer tertukar.
            if (state.playerToAgent(maximisingPlayer) != maximisingPlayer) {
                // Jika tertukar, maka heuristicScore akan ditukar.
                heuristicScore = -heuristicScore;
            }

            // Mengembalikan fungsi evaluasi yang didapat dari perhitungan.
            return heuristicScore;
        }

        // Variabel Game dari game.Game Ludii berisi referensi game yang dimainkan dari context saat ini.
        final Game game = context.game();
        // Variabel untuk menyimpan pemain yang akan bergerak.
        final int mover = state.playerToAgent(state.mover());

        // Variabel untuk menyimpan list dari move yang bisa dicapai di root saat ini, menggunakan FastArrayList dari main.collection.FastArrayList Ludii.
        final FastArrayList<Move> legalMoves = game.moves(context).moves();
        // Variabel untuk menyimpan size dari legalMoves.
        final int numLegalMoves = legalMoves.size();
        // Variabel untuk menyimpan alpha selama pencarian.
        float alpha = inAlpha;
        // Variabel untuk menyimpan beta selama pencarian.
        float beta = inBeta;

        // Mengecek apakah pemain yang akan bergerak adalah maximisingPlayer.
        if (mover == maximisingPlayer) {
            // Variabel untuk menyimpan score tertinggi selama pencarian.
            float score = ALPHA_INIT;

            // Memulai iterasi penelusuran move yang tersedia pada tiap tingkat kedalaman pencarian.
            for (int i = 0; i < numLegalMoves; ++i) {
                // Variabel untuk menyimpan copy dari Context game yang sedang dimainkan.
                final Context copyContext = new Context(context);
                // Variabel untuk menyimpan move yang sedang dicapai saat pencarian.
                final Move m = legalMoves.get(i);
                // Method untuk meng-apply move yang terpilih saat ini, menggunakan method apply(context, move) dari Game di game.Game Ludii.
                game.apply(copyContext, m);
                // Variabel untuk menyimpan hasil evaluasi tiap state yang di dapat dengan method alphaBeta.
                final float value = alphaBeta(copyContext, depth - 1, alpha, beta, maximisingPlayer, stopTime);

                // Mengecek apakah waktu pencarian sampai sekarang sudah lebih dari atau sama dengan stopTime.
                if (System.currentTimeMillis() >= stopTime) {
                    // Mengembalikan 0(menandakan pencarian belum selesai).
                    return 0;
                }

                // Mengecek apakah hasil evaluasi yang didapat lebih dari score saat ini. 
                if (value > score) {
                    // Mengisi score saat ini dengan value.
                    score = value;
                }

                // Mengecek apakah alpha lebih rendah dari score saat ini, untuk menandakan batas bawah baru.
                if (score > alpha) {
                    // Mengisi alpha dengan score.
                    alpha = score;
                }

                // Mengecek apakah beta lebih rendah atau sama dengan alpha, karena jika beta sudah lebih rendah dari alpha tidak ada gunanya mencari lagi.
                if (alpha >= beta) {
                    // Hentikan iterasi
                    break;
                }
            }

            // Mengembalikan score terbaik yang didapat selama pencarian.
            return score;
        } else {
            // Variabel untuk menyimpan score tertinggi selama pencarian.
            float score = BETA_INIT;

            // Memulai iterasi penelusuran move yang tersedia pada tiap tingkat kedalaman pencarian.
            for (int i = 0; i < numLegalMoves; ++i) {
                // Variabel untuk menyimpan copy dari Context game yang sedang dimainkan.
                final Context copyContext = new Context(context);
                // Variabel untuk menyimpan move yang sedang dicapai saat pencarian.
                final Move m = legalMoves.get(i);
                // Method untuk meng-apply move yang terpilih saat ini, menggunakan method apply(context, move) dari Game di game.Game Ludii.
                game.apply(copyContext, m);
                // Variabel untuk menyimpan hasil evaluasi tiap state yang di dapat dengan method alphaBeta.
                final float value = alphaBeta(copyContext, depth - 1, alpha, beta, maximisingPlayer, stopTime);

                // Mengecek apakah waktu pencarian sampai sekarang sudah lebih dari atau sama dengan stopTime.
                if (System.currentTimeMillis() >= stopTime) {
                    // Mengembalikan 0(menandakan pencarian belum selesai).
                    return 0;
                }

                // Mengecek apakah hasil evaluasi yang didapat kurang dari score saat ini. 
                if (value < score) {
                    // Mengisi score saat ini dengan value.
                    score = value;
                }

                // Mengecek apakah beta lebih besar dari score saat ini, untuk menandakan batas atas baru.
                if (score < beta) {
                    // Mengisi beta dengan score.
                    beta = score;
                }

                // Mengecek apakah beta lebih rendah atau sama dengan alpha, karena jika beta sudah lebih rendah dari alpha tidak ada gunanya mencari lagi.
                if (alpha >= beta) {
                    // Hentikan iterasi
                    break;
                }
            }

            // Mengembalikan score terbaik yang didapat selama pencarian.
            return score;
        }
    }

    /**
     * Method dari util.AI Ludii, untuk melakukan inisiasi AI.
     *
     * @param game Variabel Game dari game.Game Ludii berisi referensi game yang
     * dimainkan.
     * @param playerID Variabel untuk menyimpan player yang menggunakan agent.
     *
     * @return void
     */
    @Override
    public void initAI(final Game game, final int playerID) {
        // Menginisialisasi heuristic yang sudah kita buat.
        heuristicValueFunction = new Heuristics();
    }
}

/**
 * Kelas Heuristics merupakan kelas untuk menghitung nilai evaluasi dari tiap state.
 */
class Heuristics {

    // Variabel yang kita gunakan untuk menginisialisasi alpha (hampir seperti min tak hingga).
    private static final float ALPHA_INIT = -1000000.f;

    // Variabel yang kita gunakan untuk menginisialisasi beta (hampir seperti tak hingga).
    private static final float BETA_INIT = -ALPHA_INIT;

    /**
     * Method untuk menghitung nilai evaluasi.
     *
     * @param context Variabel Context dari util.Context Ludii berisi context
     * (termasuk state) dari game yang dimainkan.
     * @param player Variabel untuk menyimpan player yang harus dihitung.
     *
     * @return nilai evaluasi yang sudah dihitung.
     */
    public float computeValue(Context context, int player) {
        // Variabel untuk menyimpan value hasil perhitungan.
        float value = 0;

        // Memulai iterasi untuk mencari didalam container state(Variabel ContainerState dari util.state.containerState.ContainerState di Ludii).
        for (final ContainerState containerState : context.state().containerStates()) {

            // Memeriksa apakah player 1 atau 2.
            if (player == 1) {
                // Jika player 1, maka baris menang kita di 8, dan baris kalah kita di 1.
                // Memulai iterasi untuk tiap cell di board.
                for (int i = 0; i < 64; i++) {
                    // Mengecek apakah ada pion di cell ke-i milik player 1, 2 atau tidak ada pion, dengan method whoCell(index) dari ContainerState di util.state.containerState.ContainerState Ludii.
                    if (containerState.whoCell(i) == 1) {
                        // Jika milik player 1, maka kita menambah value dengan 1-8 sesuai dengan baris dimana pion yang dimiliki berada dan jaraknya dengan jarak menang.
                        // Jika sudah sampai baris ujung, maka diberikan value BETA_INIT(tak hingga) dan langsung kita break.
                        if (i < 8) {
                            // Jika dibaris 1, kita menambah value dengan 1.
                            value += 1;
                        } else if (i < 16) {
                            // Jika dibaris 2, kita menambah value dengan 2.
                            value += 2;
                        } else if (i < 24) {
                            // Jika dibaris 3, kita menambah value dengan 3.
                            value += 3;
                        } else if (i < 32) {
                            // Jika dibaris 4, kita menambah value dengan 4.
                            value += 4;
                        } else if (i < 40) {
                            // Jika dibaris 5, kita menambah value dengan 5.
                            value += 5;
                        } else if (i < 48) {
                            // Jika dibaris 6, kita menambah value dengan 6.
                            value += 6;
                        } else if (i < 56) {
                            // Jika dibaris 7, kita menambah value dengan 7.
                            value += 7;
                        } else {
                            // Jika dibaris 8, kita sudah menang, maka kita beri nilai tak hingga.
                            value = BETA_INIT;
                            // Menghentikan iterasi.
                            break;
                        }
                    } else if (containerState.whoCell(i) == 0) {
                        // Jika kosong, maka kita menambah value dengan 1 untuk tiap baris kosong.
                        value += 1;
                    } else {
                        // Jika milik player 2, maka kita mengurangi value dengan 1-8 sesuai dengan baris dimana pion yang dimiliki berada dan jaraknya dengan jarak kalah.
                        // Jika sudah sampai baris ujung, maka diberikan value ALPHA_INIT(min tak hingga) dan langsung kita break.
                        if (i < 8) {
                            // Jika musuh dibaris 1, kita sudah kalah, maka kita beri nilai min tak hingga.
                            value = ALPHA_INIT;
                            // Menghentikan iterasi.
                            break;
                        } else if (i < 16) {
                            // Jika musuh dibaris 2, kita mengurangi value dengan 7.
                            value -= 7;
                        } else if (i < 24) {
                            // Jika musuh dibaris 3, kita mengurangi value dengan 6.
                            value -= 6;
                        } else if (i < 32) {
                            // Jika musuh dibaris 4, kita mengurangi value dengan 5.
                            value -= 5;
                        } else if (i < 40) {
                            // Jika musuh dibaris 5, kita mengurangi value dengan 4.
                            value -= 4;
                        } else if (i < 48) {
                            // Jika musuh dibaris 6, kita mengurangi value dengan 3.
                            value -= 3;
                        } else if (i < 56) {
                            // Jika musuh dibaris 7, kita mengurangi value dengan 2.
                            value -= 2;
                        } else {
                            // Jika musuh dibaris 8, kita mengurangi value dengan 1.
                            value -= 1;
                        }
                    }
                }
            } else {
                // Jika player 2, maka baris menang kita di 1, dan baris kalah kita di 8.
                //Memulai iterasi untuk tiap cell di board.
                for (int i = 0; i < 64; i++) {
                    // Mengecek apakah ada pion di cell ke-i milik player 1, 2 atau tidak ada pion, dengan method whoCell(index) dari ContainerState di util.state.containerState.ContainerState Ludii.
                    if (containerState.whoCell(i) == 1) {
                        // Jika milik player 1, maka kita mengurangi value dengan 1-8 sesuai dengan baris dimana pion yang dimiliki berada dan jaraknya dengan jarak kalah.
                        // Jika sudah sampai baris ujung, maka diberikan value ALPHA_INIT(min tak hingga) dan langsung kita break.
                        if (i < 8) {
                            // Jika musuh dibaris 1, kita mengurangi value dengan 1.
                            value -= 1;
                        } else if (i < 16) {
                            // Jika musuh dibaris 2, kita mengurangi value dengan 2.
                            value -= 2;
                        } else if (i < 24) {
                            // Jika musuh dibaris 3, kita mengurangi value dengan 3.
                            value -= 3;
                        } else if (i < 32) {
                            // Jika musuh dibaris 4, kita mengurangi value dengan 4.
                            value -= 4;
                        } else if (i < 40) {
                            // Jika musuh dibaris 5, kita mengurangi value dengan 5.
                            value -= 5;
                        } else if (i < 48) {
                            // Jika musuh dibaris 6, kita mengurangi value dengan 6.
                            value -= 6;
                        } else if (i < 56) {
                            // Jika musuh dibaris 7, kita mengurangi value dengan 7.
                            value -= 7;
                        } else {
                            // Jika musuh dibaris 8, kita sudah kalah, maka kita beri nilai min tak hingga.
                            value = ALPHA_INIT;
                            // Menghentikan iterasi.
                            break;
                        }
                    } else if (containerState.whoCell(i) == 0) {
                        // Jika kosong, maka kita menambah value dengan 1 untuk tiap baris kosong.
                        value += 1;
                    } else {
                        // Jika milik player 2, maka kita menambah value dengan 1-8 sesuai dengan baris dimana pion yang dimiliki berada dan jaraknya dengan jarak menang.
                        // Jika sudah sampai baris ujung, maka diberikan value BETA_INIT(tak hingga) dan langsung kita break.
                        if (i < 8) {
                            // Jika dibaris 1, kita sudah menang, maka kita beri nilai tak hingga.
                            value = BETA_INIT;
                            // Menghentikan iterasi.
                            break;
                        } else if (i < 16) {
                            // Jika dibaris 2, kita menambah value dengan 7.
                            value += 7;
                        } else if (i < 24) {
                            // Jika dibaris 3, kita menambah value dengan 6.
                            value += 6;
                        } else if (i < 32) {
                            // Jika dibaris 4, kita menambah value dengan 5.
                            value += 5;
                        } else if (i < 40) {
                            // Jika dibaris 5, kita menambah value dengan 4.
                            value += 4;
                        } else if (i < 48) {
                            // Jika dibaris 6, kita menambah value dengan 3.
                            value += 3;
                        } else if (i < 56) {
                            // Jika dibaris 7, kita menambah value dengan 2.
                            value += 2;
                        } else {
                            // Jika dibaris 8, kita menambah value dengan 1.
                            value += 1;
                        }
                    }
                }
            }
        }

        // Mengembalikan value hasil perhitungan.
        return value;
    }
}
