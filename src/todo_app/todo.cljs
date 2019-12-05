(ns todo-app.todo
 (:require [reagent.core :as r]
           [garden.core :refer [css]]))

(defonce todos (r/atom (sorted-map)))
(defonce counter (r/atom 0))

(defn add-todo [text]
  (let [id (swap! counter inc)]
    (swap! todos assoc id {:id id :text text :done false :notes "" :due-date ""})))

(defn toggle-all []
  [:button "Toggle All"])

(defn todo-input []
  (let [input (r/atom "")]
    (fn []
      [:form.input-form
       [:input.todo-input {:type "text" :value @input :on-change #(reset! input (-> % .-target .-value))}] 
       [:button.add-btn {:on-click (fn [e] (.preventDefault e) (add-todo @input) (reset! input ""))} [:i.fas.fa-plus]  " ADD"]])))

(defn todo-item [{:keys [id text done notes due-date]}]
  (let [is-editing (r/atom false)
        edit-input (r/atom text) 
        show-date-input (r/atom false) 
        edit-date (r/atom due-date)
        show-notes-input (r/atom false)
        edit-notes (r/atom notes)]
    (fn [{:keys [id text done notes due-date]}]
      [:div {:id (.toString id)}
       (if-not @is-editing
         [:div.todo-item
          [:button.td-itm-check-btn {:on-click (fn [e]
                                                 (.preventDefault e)
                                                 (swap! todos update-in [id :done] not))}
           (if done [:i.far.fa-check-circle] [:i.far.fa-circle])]
          [:span.td-itm-text text]
          [:div.td-itm-actions
           [:button.act-btn {:on-click (fn [e] (reset! show-date-input true))}
            [:i.far.fa-clock]
            (when (not-empty due-date) [:span.td-itm-due-date due-date])]
           [:button.act-btn {:on-click (fn [e] (reset! show-notes-input true))} [:i.far.fa-sticky-note]]
           [:button.act-btn {:on-click (fn [e] (reset! is-editing true))} [:i.far.fa-edit]]
           [:button.act-btn {:on-click (fn [e] (swap! todos dissoc id))} [:i.far.fa-trash-alt]]]
          (when @show-date-input
            [:form
             [:input {:type "date" :value @edit-date :on-change #(reset! edit-date (-> % .-target .-value))}]
             [:button {:on-click (fn [e]
                                   (.preventDefault e)
                                   (swap! todos assoc-in [id :due-date] @edit-date)
                                   (reset! show-date-input false))} [:i.fas.fa-check]]
             [:button {:on-click (fn [e] (.preventDefault e) (reset! show-date-input false))} [:i.fas.fa-times]]])
          (when @show-notes-input
            [:form
             [:textarea {:on-change #(reset! edit-notes (-> % .-target .-value))} @edit-notes]
             [:button {:on-click (fn [e]
                                   (.preventDefault e)
                                   (swap! todos assoc-in [id :notes] @edit-notes)
                                   (reset! show-notes-input false))} [:i.fas.fa-check]]
             [:button {:on-click (fn [e] (.preventDefault e) (reset! show-notes-input false))} [:i.fas.fa-times]]])]
         [:div.todo-item-editing
          [:form
           [:input {:type "text" :value @edit-input :on-change #(reset! edit-input (-> % .-target .-value))}]
           [:button {:on-click (fn [e]
                                 (.preventDefault e)
                                 (reset! is-editing false)
                                 (swap! todos assoc-in [id :text] @edit-input))} [:i.fas.fa-check]]
           [:button {:on-click (fn [e] (.preventDefault e) (reset! is-editing false))} [:i.fas.fa-times]]]])])))

(defn todo-app []
  [:div.app
   [:h2#title "Todo App"]
   [todo-input]
   (for [todo (vals @todos)] ^{:key (:id todo)} [todo-item todo])])

(defn mount-root []
  (r/render [todo-app] js/document.body))