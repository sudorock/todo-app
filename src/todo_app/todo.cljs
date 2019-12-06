(ns todo-app.todo
 (:require [reagent.core :as r]
           [garden.core :refer [css]]))

(defonce todos (r/atom (sorted-map)))
(defonce counter (r/atom 0))

(defn add-todo [text]
  (let [id (swap! counter inc)]
    (when (not-empty text)
      (swap! todos assoc id {:id id :text text :done false :notes "" :due-date ""}))))

(defn toggle-all []
  [:button "Toggle All"])

(defn todo-input []
  (let [input (r/atom "")]
    (fn []
      [:form.input-form
       [:input.todo-input {:type "text" :value @input :on-change #(reset! input (-> % .-target .-value))}] 
       [:button.add-btn {:on-click (fn [e] (.preventDefault e) (add-todo @input) (reset! input ""))} [:i.fas.fa-plus {:style {:margin-right 10}}]  "ADD"]])))

(defn todo-item [{:keys [id text done notes due-date]}]
  (let [is-editing (r/atom false)
        edit-input (r/atom text) 
        show-date-input (r/atom false) 
        edit-date (r/atom due-date)
        show-notes-input (r/atom false)
        edit-notes (r/atom notes)]
    (fn [{:keys [id text done notes due-date]}]
      [:div.td-itm-container {:id (.toString id)}
       (if-not @is-editing
         [:div.todo-item
          [:div.td-itm-check {:on-click (fn [e] (swap! todos update-in [id :done] not))}
           (if done [:i.far.fa-check-circle.done] [:i.far.fa-circle.not-done])]
          [:span.td-itm-text text]
          [:div.td-itm-actions
           [:div.btn-container
            [:button.act-btn.date {:on-click (fn [e] (swap! show-date-input not))}
             [:i.far.fa-clock]
             (when (not-empty due-date) [:span.td-itm-due-date due-date])]
            (when @show-date-input
              [:form.td-itm-date-form
               [:input.td-date-input {:type "date"
                                      :value @edit-date
                                      :on-change #(reset! edit-date (-> % .-target .-value))}]
               [:button.dialog-btn.ok {:on-click (fn [e]
                                     (.preventDefault e)
                                     (swap! todos assoc-in [id :due-date] @edit-date)
                                     (reset! show-date-input false))} [:i.fas.fa-check]]
               [:button.dialog-btn.cancel {:on-click (fn [e] (.preventDefault e) (reset! show-date-input false))} [:i.fas.fa-times]]])]
           [:div.btn-container
            [:button.act-btn.note {:on-click (fn [e] (swap! show-notes-input not))} [:i.far.fa-sticky-note]]
            (when @show-notes-input
              [:form.td-itm-note-form
               [:textarea.note-txt-area {:value @edit-notes :form "note-input" :on-change #(reset! edit-notes (-> % .-target .-value))}]
               [:div.td-itm-note-act-btns
                [:button.dialog-btn.ok {:on-click (fn [e]
                                      (.preventDefault e)
                                      (swap! todos assoc-in [id :notes] @edit-notes)
                                      (reset! show-notes-input false))} [:i.fas.fa-check]]
                [:button.dialog-btn.cancel {:on-click (fn [e] (.preventDefault e) (reset! show-notes-input false))} [:i.fas.fa-times]]]])]
           [:button.act-btn.edit {:on-click (fn [e] (reset! is-editing true))} [:i.far.fa-edit]]
           [:button.act-btn.delete {:on-click (fn [e] (swap! todos dissoc id))} [:i.far.fa-trash-alt]]]]
         [:div.todo-item-editing
          [:form.td-itm-edit-form
           [:input.td-itm-edit-input 
            {:type "text" :value @edit-input :on-change #(reset! edit-input (-> % .-target .-value))}]
           [:div.td-itm-edit-actions
            [:button.td-itm-editing-act-btn.ok {:on-click (fn [e]
                                  (.preventDefault e)
                                  (reset! is-editing false)
                                  (swap! todos assoc-in [id :text] @edit-input))} [:i.fas.fa-check]]
            [:button.td-itm-editing-act-btn.cancel {:on-click (fn [e] (.preventDefault e) (reset! is-editing false))} [:i.fas.fa-times]]]]])])))

(defn todo-app []
  [:div.app
   [:h1#title "Todo App"]
   [todo-input]
   (for [todo (vals @todos)] ^{:key (:id todo)} [todo-item todo])])

(defn mount-root []
  (r/render [todo-app] (.getElementById js/document "root")))